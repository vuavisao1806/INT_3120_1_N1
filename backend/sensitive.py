from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from connection import get_openai_connection
import base64
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
    connection = get_openai_connection()
    try:
        response = connection.moderations.create(
            model="omni-moderation-latest",
            input=body.text,
        )
    except Exception as error:
        raise HTTPException(status_code=500, detail=f"The error occurs when checking sensitive text: {error}")
    result: SensitiveTextRespond = SensitiveTextRespond(is_sensitive = bool(response.results[0].flagged))
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