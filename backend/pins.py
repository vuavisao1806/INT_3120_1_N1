from fastapi import APIRouter
from connection import get_database_connection
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
def insert(body: InsertPinRequest):
    connection = get_database_connection()
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
    connection = get_database_connection()
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

# ==================================================
#       LẤY DANH SÁCH GHIM TRONG VÙNG BÁN KÍNH
# ==================================================  

class GetPinsInRadiusRequest(BaseModel):
    center_lat: float
    center_lng: float
    radius_meters: float


@router.post("/get/in-radius")
def get_pins_in_radius(body: GetPinsInRadiusRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            # Haversine: tính khoảng cách giữa (center_lat, center_lng) và (p.latitude, p.longitude)
            # 6371000: bán kính Trái Đất ~ 6,371km (đơn vị mét)
            cur.execute(
                """
                SELECT *
                FROM pins p
                WHERE (
                    6371000 * acos(
                        cos(radians(%s)) * cos(radians(p.latitude::double precision)) *
                        cos(radians(p.longitude::double precision) - radians(%s)) +
                        sin(radians(%s)) * sin(radians(p.latitude::double precision))
                    )
                  ) <= %s;
                """,
                (
                    body.center_lat,        # %s thứ 2: lat tâm
                    body.center_lng,        # %s thứ 3: lng tâm
                    body.center_lat,        # %s thứ 4: lat tâm
                    body.radius_meters      # %s thứ 5: bán kính (m)
                )
            )

            pins = cur.fetchall()
            return pins
    finally:
        connection.close()
