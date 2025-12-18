import os
import base64
import requests
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from connection import get_openai_connection
from fastapi import File, UploadFile

router = APIRouter(
    prefix="/sensitive",
    tags=["sensitive"]
)

class CheckSensitiveTextRequest(BaseModel):
    text: str
    
    
class SensitiveTextRespond(BaseModel):
    is_sensitive: bool

@router.post("/text")
def isSensitiveText(body: CheckSensitiveTextRequest):
    if (body.text == ""):
        return SensitiveTextRespond(is_sensitive=False)
    connection = get_openai_connection()
    try:
        response = connection.moderations.create(
            model="omni-moderation-latest",
            input=body.text,
        )
    except Exception as error:
        raise HTTPException(status_code=500, detail=f"The error occurs when checking sensitive text: {error}")
    result: SensitiveTextRespond = SensitiveTextRespond(is_sensitive = bool(response.results[0].flagged))
    if (result.is_sensitive):
        return result

    body.text = translate_to_english(body.text)
    # print(body.text)
    try:
        response = connection.moderations.create(
            model="omni-moderation-latest",
            input=body.text,
        )
    except Exception as error:
        raise HTTPException(status_code=500, detail=f"The error occurs when checking sensitive text: {error}")
    result = SensitiveTextRespond(is_sensitive = bool(response.results[0].flagged))
    return result

class SensitiveTextRespond(BaseModel):
    is_sensitive: bool

def encode_image_to_base64(file_bytes: bytes) -> str:
    return base64.b64encode(file_bytes).decode("utf-8")

@router.post("/image", response_model=SensitiveTextRespond)
async def moderate_image(file: UploadFile = File(...)):
    
    connection = get_openai_connection()
    print(connection.api_key)
    try:
        file_bytes = await file.read()
        base64_image = encode_image_to_base64(file_bytes)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi khi đọc hoặc mã hóa tệp: {e}")

    try:
        response = connection.moderations.create(
            model="omni-moderation-latest",
            input=[
                {
                    "type": "image_url",
                    "image_url": {
                        "url": f"data:{file.content_type};base64,{base64_image}"
                    },
                }
            ],
        )
        is_flagged = bool(response.results[0].flagged)
        
    except Exception as error:
        raise HTTPException(status_code=500, detail=f"Lỗi khi kiểm tra hình ảnh: {error}")
    
    result: SensitiveTextRespond = SensitiveTextRespond(is_sensitive = is_flagged)
    return result

def translate_to_english(text: str) -> str:
    URL = "https://translation.googleapis.com/language/translate/v2"
    API_KEY = os.environ["TRANSLATE_API_KEY"]
    payload = {"q": text, "target": "en", "format": "text"}
    try:
        r = requests.post(URL, params={"key": API_KEY}, json=payload, timeout=10)
        r.raise_for_status()
        return r.json()["data"]["translations"][0]["translatedText"]
    except requests.RequestException as e:
        raise HTTPException(status_code=502, detail=f"Translate error: {e}")