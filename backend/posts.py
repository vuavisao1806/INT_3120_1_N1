import os
from fastapi import APIRouter, File, HTTPException, UploadFile
from connection import get_database_connection
from pydantic import BaseModel
from psycopg2.extras import RealDictCursor
import uuid
from supabase import create_client, Client

router = APIRouter(
    prefix="/posts",
    tags=["posts"]
)

# ==================================================
#              TẠO MỘT POST
# ==================================================


from fastapi import HTTPException
from pydantic import BaseModel

class InsertPostRequest(BaseModel):
    pin_id: int
    user_id: int
    title: str
    body: str
    image_url: str
    status: str

class InsertPostSuccess(BaseModel):
    insert_post_success: bool = True
    post_id: int
@router.post("/insert", response_model=InsertPostSuccess)
def insert_post(body: InsertPostRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                INSERT INTO posts (pin_id, user_id, title, body, image_url, status)
                VALUES (%s, %s, %s, %s, %s, %s)
                RETURNING post_id
                """,
                (body.pin_id, body.user_id, body.title, body.body, body.image_url, body.status),
            )
            post_id = cur.fetchone()["post_id"]

            cur.execute(
                """
                INSERT INTO user_pins (user_id, pin_id)
                VALUES (%s, %s)
                ON CONFLICT (user_id, pin_id) DO NOTHING;
                """,
                (body.user_id, body.pin_id),
            )

        connection.commit()
        return InsertPostSuccess(post_id=post_id)

    except Exception as e:
        connection.rollback()
        raise HTTPException(status_code=500, detail=f"DB error: {e}")
    finally:
        connection.close()




# ==================================================
#       Tương tác comment với bài viết
# ==================================================  

class CreateCommentRequest(BaseModel):
    post_id: int
    user_id: int
    content: str
    child_of_comment_id: int | None = None

@router.post("/comment")
def create_comment(body: CreateCommentRequest):
    connection = get_database_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Insert comment
                cur.execute(
                    """
                    INSERT INTO comments (post_id, user_id, content, child_of_comment_id)
                    VALUES (%s, %s, %s, %s)
                    RETURNING comment_id, created_at;
                    """,
                    (body.post_id, body.user_id, body.content, body.child_of_comment_id,)
                )

                # 2. Update comment_count
                cur.execute(
                    """
                    UPDATE posts
                    SET comment_count = comment_count + 1
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Update users_tags for all tags of that post
                cur.execute(
                    """
                    INSERT INTO users_tags (user_id, tag_id, cnt)
                    SELECT %s AS user_id, pt.tag_id, 1
                    FROM post_tags pt
                    WHERE pt.post_id = %s
                    ON CONFLICT (user_id, tag_id)
                    DO UPDATE SET cnt = users_tags.cnt + 1;
                    """,
                    (body.user_id, body.post_id)
                )
    finally:
        connection.close()

# ==================================================
#       Tương tác thả tim với bài viết
# ================================================== 

class ReactionRequest(BaseModel):
    post_id: int
    user_id: int

@router.post("/react")
def react_post(body: ReactionRequest):
    connection = get_database_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Insert reaction nếu chưa có
                cur.execute(
                    """
                    INSERT INTO reactions (post_id, user_id)
                    VALUES (%s, %s)
                    ON CONFLICT (post_id, user_id) DO NOTHING;
                    """,
                    (body.post_id, body.user_id)
                )

                # Nếu không insert được (đã tim rồi) thì không cần cộng nữa
                if cur.rowcount == 0:
                    return {"status": "already_reacted"}

                # 2. Tăng reaction_count
                cur.execute(
                    """
                    UPDATE posts
                    SET reaction_count = reaction_count + 1
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Update users_tags cho tất cả tag của post
                cur.execute(
                    """
                    INSERT INTO users_tags (user_id, tag_id, cnt)
                    SELECT %s AS user_id, pt.tag_id, 1
                    FROM post_tags pt
                    WHERE pt.post_id = %s
                    ON CONFLICT (user_id, tag_id)
                    DO UPDATE SET cnt = users_tags.cnt + 1;
                    """,
                    (body.user_id, body.post_id)
                )
    finally:
        connection.close()

class CheckReactionRequest(BaseModel):
    post_id: int
    user_id: int

class HaveReaction(BaseModel):
    have_reaction: bool = True

@router.post("/react/check")
def check_react_post(body: CheckReactionRequest):
    connection = get_database_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                cur.execute(
                    """
                    SELECT * FROM reactions
                    WHERE post_id = %s AND user_id = %s;
                    """,
                    (body.post_id, body.user_id)
                )
                status: HaveReaction = HaveReaction()
                if (cur.rowcount == 0):
                    status.have_reaction = False
                return status
    finally:
        connection.close()


# ==================================================
#       Tương tác hủy thả tim với bài viết
# ================================================== 

class CancelReactionRequest(BaseModel):
    post_id: int
    user_id: int

@router.post("/react/cancel")
def cancel_react_post(body: CancelReactionRequest):
    connection = get_database_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Xóa reaction (nếu có)
                cur.execute(
                    """
                    DELETE FROM reactions
                    WHERE post_id = %s AND user_id = %s;
                    """,
                    (body.post_id, body.user_id)
                )

                # Nếu không xóa được dòng nào thì thôi, không làm gì thêm
                if cur.rowcount == 0:
                    return

                # 2. Giảm reaction_count của post (nếu có cột này)
                cur.execute(
                    """
                    UPDATE posts
                    SET reaction_count = GREATEST(reaction_count - 1, 0)
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Giảm cnt trong users_tags cho các tag của post đó
                #    (1 lần hủy tim => trừ 1 điểm cho mỗi tag)
                cur.execute(
                    """
                    UPDATE users_tags ut
                    SET cnt = GREATEST(ut.cnt - 1, 0)
                    FROM post_tags pt
                    WHERE ut.user_id = %s
                      AND ut.tag_id = pt.tag_id
                      AND pt.post_id = %s;
                    """,
                    (body.user_id, body.post_id)
                )

                # (optional) Nếu muốn xóa luôn những dòng cnt <= 0:
                # cur.execute(
                #     "DELETE FROM users_tags WHERE user_id = %s AND cnt <= 0;",
                #     (body.user_id,)
                # )

        # tạm thời không trả về gì
        return
    finally:
        connection.close()


# ==================================================
#       Tương tác hủy comment với bài viết
# ==================================================  

class CancelCommentRequest(BaseModel):
    comment_id: int
    user_id: int
@router.post("/comment/cancel")
def cancel_comment(body: CancelCommentRequest):
    connection = get_database_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Lấy post_id của comment để còn update posts + users_tags
                cur.execute(
                    """
                    SELECT post_id
                    FROM comments
                    WHERE comment_id = %s AND user_id = %s;
                    """,
                    (body.comment_id, body.user_id)
                )
                row = cur.fetchone()

                # Không tìm thấy comment (hoặc không thuộc user này) thì thôi
                if row is None:
                    return

                post_id = row[0]

                # 2. Xóa comment
                cur.execute(
                    """
                    DELETE FROM comments
                    WHERE comment_id = %s AND user_id = %s;
                    """,
                    (body.comment_id, body.user_id)
                )

                if cur.rowcount == 0:
                    # Về lý thuyết không xảy ra vì vừa SELECT, nhưng cứ check cho chắc
                    return

                # 3. Giảm comment_count của post (nếu có cột này)
                cur.execute(
                    """
                    UPDATE posts
                    SET comment_count = GREATEST(comment_count - 1, 0)
                    WHERE post_id = %s;
                    """,
                    (post_id,)
                )

                # 4. Giảm cnt trong users_tags cho các tag của post đó
                #    (mỗi lần hủy 1 comment => trừ 1 điểm cho mỗi tag)
                cur.execute(
                    """
                    UPDATE users_tags ut
                    SET cnt = GREATEST(ut.cnt - 1, 0)
                    FROM post_tags pt
                    WHERE ut.user_id = %s
                      AND ut.tag_id = pt.tag_id
                      AND pt.post_id = %s;
                    """,
                    (body.user_id, post_id)
                )

                # (optional) Xóa dòng users_tags nếu cnt <= 0
                # cur.execute(
                #     "DELETE FROM users_tags WHERE user_id = %s AND cnt <= 0;",
                #     (body.user_id,)
                # )

        # tạm thời không trả về gì
        return
    finally:
        connection.close()


# ==================================================
#       Lấy 1 post theo post_id 
# ================================================== 

class GetPostRequest(BaseModel):
    post_id: int

@router.post("/get")
def get_post(body: GetPostRequest):
    connection = get_database_connection()
    try:
        # dùng RealDictCursor để trả về dạng dict -> FastAPI tự convert sang JSON đẹp
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT 
                    p.*, 
                    u.user_name,
                    u.avatar_url
                FROM posts p
                JOIN users u ON u.user_id = p.user_id
                WHERE p.post_id = %s;
                """,
                (body.post_id,)
            )
            row = cur.fetchone()
            return row
    finally:
        connection.close()

# ==================================================
#       Lấy danh sách comment của 1 bài viết (JOIN users)
# ==================================================
class GetPostCommentsRequest(BaseModel):
    post_id: int

@router.post("/get/comments")
def get_comments_of_post(body: GetPostCommentsRequest):
    connection = get_database_connection()
    try:
        # dùng RealDictCursor để trả về dict thay vì tuple
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT 
                    c.comment_id,
                    c.post_id,
                    c.user_id,
                    c.content,
                    c.created_at,
                    c.child_of_comment_id,
                    u.user_name,      
                    u.avatar_url   
                FROM comments c
                JOIN users u ON u.user_id = c.user_id
                WHERE c.post_id = %s
                ORDER BY c.created_at ASC;
                """,
                (body.post_id,)
            )
            comments = cur.fetchall()
            return comments
    finally:
        connection.close()


class GetPostTagsRequest(BaseModel):
    post_id: int

@router.post("/get/tags")
def get_post_tags(body: GetPostTagsRequest):
    connection = get_database_connection()
    try:
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT t.tag_id, t.name
                FROM post_tags pt
                JOIN tags t ON t.tag_id = pt.tag_id
                WHERE pt.post_id = %s
                ORDER BY t.name;
                """,
                (body.post_id,)
            )
            return cur.fetchall()
    finally:
        connection.close()



# ==================================================
#       LẤY NEWSFEED - Tổng hợp bài viết từ các pin của user
# ==================================================

class GetNewsfeedRequest(BaseModel):
    user_id: int
    limit: int = 20  # Số bài viết tối đa mỗi lần load
    offset: int = 0  # Để phân trang
    tag_name: str | None = None  # Tên tag để lọc (optional)

@router.post("/newsfeed")
def get_newsfeed(body: GetNewsfeedRequest):
    conn = get_database_connection()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            # Nếu có tag_name, lọc theo tag
            if body.tag_name:
                cur.execute(
                    """
                    SELECT
                        p.post_id,
                        p.pin_id,
                        p.title,
                        p.body,
                        p.image_url,
                        p.user_id,
                        p.status,
                        p.created_at,
                        p.reaction_count,
                        p.comment_count,
                        u.user_name,
                        u.avatar_url
                    FROM posts p
                    JOIN users u ON u.user_id = p.user_id
                    JOIN post_tags pt ON p.post_id = pt.post_id
                    JOIN tags t ON pt.tag_id = t.tag_id
                    WHERE p.pin_id IN (
                        SELECT pin_id
                        FROM user_pins
                        WHERE user_id = %s
                    )
                    AND t.name = %s
                    ORDER BY p.created_at DESC
                    LIMIT %s OFFSET %s;
                    """,
                    (body.user_id, body.tag_name, body.limit, body.offset)
                )
            else:
                # Không lọc tag, lấy tất cả
                cur.execute(
                    """
                    SELECT
                        p.post_id,
                        p.pin_id,
                        p.title,
                        p.body,
                        p.image_url,
                        p.user_id,
                        p.status,
                        p.created_at,
                        p.reaction_count,
                        p.comment_count,
                        u.user_name,
                        u.avatar_url
                    FROM posts p
                    JOIN users u ON u.user_id = p.user_id
                    WHERE p.pin_id IN (
                        SELECT pin_id
                        FROM user_pins
                        WHERE user_id = %s
                    )
                    ORDER BY p.created_at DESC
                    LIMIT %s OFFSET %s;
                    """,
                    (body.user_id, body.limit, body.offset)
                )

            posts = cur.fetchall()
            return posts
    finally:
        conn.close()
class GetPreviewPinsRequest(BaseModel):
    user_id: int

@router.post("/pinpreview")
def get_preview_pins(body: GetPreviewPinsRequest):
    conn = get_database_connection()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT
                    p.pin_id,
                    MIN(p.image_url) as image_url,
                    (SELECT COUNT(*) FROM posts p2 WHERE p2.pin_id = p.pin_id) as cnt
                FROM posts p
                JOIN users u ON u.user_id = p.user_id
                WHERE u.user_id = %s
                GROUP BY p.pin_id;
                """,
                (body.user_id,)
            )
            pins = cur.fetchall()
            return pins
    finally:
        conn.close()

class GetPostByPinIdRequest(BaseModel):
    pin_id: int

@router.post("/pinId")
def get_preview_pins(body: GetPostByPinIdRequest):
    conn = get_database_connection()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT
                    *
                FROM posts p 
                WHERE p.pin_id = %s;
                """,
                (body.pin_id,)
            )
            pins = cur.fetchall()
            return pins
    finally:
        conn.close()
        
class GetPostByPinIdRequestFromMapScreen(BaseModel):
    pin_id: int

@router.post("/pinId/mapScreen")
def get_preview_pins(body: GetPostByPinIdRequestFromMapScreen):
    conn = get_database_connection()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT
                    p.post_id,
                    p.pin_id,
                    p.title,
                    p.body,
                    p.image_url,
                    p.user_id,
                    p.status,
                    p.created_at,
                    p.reaction_count,
                    p.comment_count,
                    u.user_name,
                    u.avatar_url
                FROM posts p
                JOIN users u ON u.user_id = p.user_id
                WHERE p.pin_id IN (
                    SELECT pin_id
                    FROM user_pins
                    WHERE pin_id = %s
                )
                ORDER BY p.created_at DESC
                """,
                (body.pin_id,) # CHỈ CẦN THÊM DẤU PHẨY NÀY LÀ XONG
            )
            posts = cur.fetchall()
            return posts
    finally:
        conn.close()

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_KEY = os.getenv("SUPABASE_KEY")
BUCKET_NAME = os.getenv("SUPABASE_BUCKET", "posts")

if not SUPABASE_URL or not SUPABASE_KEY:
    raise RuntimeError("SUPABASE_URL hoặc SUPABASE_KEY chưa được cấu hình trong .env")

supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)

@router.post("/upload-image")
async def upload_image(file: UploadFile = File(...)):
    """
    Upload ảnh lên Supabase Storage và trả về 1 public URL duy nhất
    """
    print("DEBUG content_type:", file.content_type)

    # 1. Check loại file
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail=f"Chỉ chấp nhận file ảnh, content_type hiện tại: {file.content_type}",
        )


    # 2. Đọc nội dung file
    contents = await file.read()

    # 3. Giới hạn dung lượng 5MB
    if len(contents) > 5 * 1024 * 1024:
        raise HTTPException(
            status_code=400,
            detail="File quá lớn (tối đa 5MB)",
        )

    # 4. Tạo tên file unique trong bucket
    ext = file.filename.split(".")[-1]
    unique_name = f"{uuid.uuid4()}.{ext}"
    file_path = f"posts/{unique_name}"  # folder 'posts/' trong bucket

    # 5. Upload lên Supabase Storage
    try:
        result = supabase.storage.from_(BUCKET_NAME).upload(
            path=file_path,
            file=contents,
            file_options={
                "content-type": file.content_type,
                "cache-control": "3600",
                "upsert": "false",
            },
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Upload thất bại: {e}")

    # Nếu SDK trả về object có 'error'
    if isinstance(result, dict) and result.get("error"):
        raise HTTPException(status_code=500, detail=f"Upload thất bại: {result['error']}")

    # 6. Lấy public URL (bucket phải là public hoặc có policy cho phép)
    public_url = supabase.storage.from_(BUCKET_NAME).get_public_url(file_path)

    return {
        "success": True,
        "url": public_url,   # URL duy nhất bạn cần
        "path": file_path,   # nếu muốn lưu DB sau này
    }

class GetPostByUserRequest(BaseModel):
    user_id:int


@router.post("/postByUser")
def getPostByUser(body: GetPostByUserRequest):
    conn = get_database_connection()
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT
                    *
                FROM posts p
                WHERE p.user_id = %s;
                """,
                (body.user_id,)
            )
            posts = cur.fetchall()
            return posts
    finally:
        conn.close()