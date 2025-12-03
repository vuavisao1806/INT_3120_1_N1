from fastapi import APIRouter
from connection import get_connection
from pydantic import BaseModel

router = APIRouter(
    prefix="/pins",
    tags=["pins"]
)

# ==================================================
#              TẠO MỘT GHIM MỚI
# ==================================================

class InsertPinRequest(BaseModel):
    latitude: float
    longitude: float
    
class InsertPinSuccess(BaseModel):
    insert_pin_success: bool = True

@router.post("/insert")
def register(body: InsertPinRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:
            # Check username exists
            cur.execute(
                """
                INSERT INTO pins (latitude, longitude)
                VALUES (%s, %s)
                """,
                (body.latitude,body.longitude)
            )
            
        connection.commit()
        return InsertPinSuccess()

    finally:
        connection.close()
        
# ==================================================
#              LẤY DANH SÁCH GHIM THEO USER_ID
# ==================================================

class GetPinListByUserIdRequest(BaseModel):
    user_id: int

@router.post("/get/user-id")
def get_pins_by_user_id(body: GetPinListByUserIdRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                SELECT pins.*
                FROM pins
                INNER JOIN user_pins ON pins.pin_id = user_pins.pin_id
                WHERE user_pins.user_id = %s;
                """,
                (body.user_id,)  
            )
            pins = cur.fetchall()   
        return pins
    finally:
        connection.close()