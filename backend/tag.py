import os
import base64
import requests
from fastapi import APIRouter, HTTPException, UploadFile, File
from pydantic import BaseModel
from typing import List
from connection import get_database_connection
from pydantic import BaseModel, Field
import psycopg2
from fastapi import HTTPException


router = APIRouter(prefix="/tag", tags=["tag"])

VISION_API_KEY = os.getenv("VISION_API_KEY")
VISION_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate"


class TagResponse(BaseModel):
    tags: List[str]


@router.post("/label_top3", response_model=TagResponse)
async def label_top3(file: UploadFile = File(...), k: int = 3):
    """
    POST /tag/label_top3?k=3
    form-data: file=<image>
    Response: {"tags": ["tag1","tag2","tag3"]}
    """
    if not VISION_API_KEY:
        raise HTTPException(status_code=500, detail="Missing env var VISION_API_KEY")

    data = await file.read()
    if not data:
        raise HTTPException(status_code=400, detail="Empty file")

    content_b64 = base64.b64encode(data).decode("utf-8")

    payload = {
        "requests": [{
            "image": {"content": content_b64},
            "features": [{"type": "LABEL_DETECTION", "maxResults": max(10, k)}],
        }]
    }

    try:
        r = requests.post(f"{VISION_ENDPOINT}?key={VISION_API_KEY}", json=payload, timeout=60)
        if r.status_code != 200:
            raise HTTPException(status_code=r.status_code, detail=r.text)

        res = r.json()
        resp0 = (res.get("responses") or [{}])[0]
        labels = resp0.get("labelAnnotations") or []

        labels_sorted = sorted(labels, key=lambda x: x.get("score", 0.0), reverse=True)
        topk = [x.get("description") for x in labels_sorted if x.get("description")][:k]

        return TagResponse(tags=topk)

    except requests.RequestException as e:
        raise HTTPException(status_code=502, detail=str(e))
    

class AssignTagsRequest(BaseModel):
    post_id: int
    user_id: int
    tags: List[str] = Field(min_length=1)


def _norm(t: str) -> str:
    return t.strip().lower()


@router.post("/assign")
def assign_tags(req: AssignTagsRequest):
    tags = [_norm(t) for t in req.tags]
    tags = [t for t in tags if t]
    if not tags:
        raise HTTPException(status_code=400, detail="tags empty")

    # unique giữ thứ tự
    seen = set()
    tags_unique = []
    for t in tags:
        if t not in seen:
            seen.add(t)
            tags_unique.append(t)

    conn = get_database_connection()
    try:
        with conn:
            with conn.cursor() as cur:
                for tag_name in tags_unique:
                    # (1) tags: tạo tag nếu chưa có, rồi lấy tag_id
                    cur.execute(
                        """
                        INSERT INTO tags(name)
                        VALUES (%s)
                        ON CONFLICT (name) DO NOTHING
                        RETURNING tag_id
                        """,
                        (tag_name,),
                    )
                    row = cur.fetchone()
                    if row is None:
                        cur.execute("SELECT tag_id FROM tags WHERE name = %s", (tag_name,))
                        row = cur.fetchone()

                    tag_id = row["tag_id"] if isinstance(row, dict) else row[0]

                    # (2) post_tags: gắn tag vào post (nếu chưa có)
                    cur.execute(
                        """
                        INSERT INTO post_tags(post_id, tag_id)
                        VALUES (%s, %s)
                        ON CONFLICT (post_id, tag_id) DO NOTHING
                        """,
                        (req.post_id, tag_id),
                    )

                    # (3) users_tags: tăng cnt nếu đã có, chưa có thì tạo cnt=1
                    cur.execute(
                        """
                        INSERT INTO users_tags(user_id, tag_id, cnt)
                        VALUES (%s, %s, 1)
                        ON CONFLICT (user_id, tag_id)
                        DO UPDATE SET cnt = users_tags.cnt + 1
                        """,
                        (req.user_id, tag_id),
                    )

    except psycopg2.Error as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()
