from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from connection import get_openai_connection


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