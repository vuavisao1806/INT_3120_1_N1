from fastapi import APIRouter, HTTPException
from connection import get_database_connection
from pydantic import BaseModel
import random
from math import atan2, degrees

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

# ==================================================
#   TÌM HOẶC TẠO PIN DựA TRÊN TỌA ĐỘ
# ==================================================

class PinByCoordRequest(BaseModel):
    center_lat: float
    center_lng: float
    radius_meters: float = 50.0  # Bán kính mặc định 50m để tìm pin gần nhất

class PinByCoordResponse(BaseModel):
    pin_id: int
    is_new_pin: bool  # True nếu tạo pin mới, False nếu dùng pin có sẵn

@router.post("/get-or-create-by-coord")
def add_post_into_pin_by_coord(body: PinByCoordRequest):
    """
    Tìm pin gần nhất trong bán kính cho trước.
    Nếu không tìm thấy -> tạo pin mới tại tọa độ đó.
    Trả về pin_id để sử dụng khi tạo post.
    """
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            # 1. Tìm pin gần nhất trong bán kính
            cur.execute(
                """
                SELECT 
                    p.pin_id,
                    (
                        6371000 * acos(
                            cos(radians(%s)) * cos(radians(p.latitude::double precision)) *
                            cos(radians(p.longitude::double precision) - radians(%s)) +
                            sin(radians(%s)) * sin(radians(p.latitude::double precision))
                        )
                    ) AS distance_meters
                FROM pins p
                WHERE (
                    6371000 * acos(
                        cos(radians(%s)) * cos(radians(p.latitude::double precision)) *
                        cos(radians(p.longitude::double precision) - radians(%s)) +
                        sin(radians(%s)) * sin(radians(p.latitude::double precision))
                    )
                ) <= %s
                ORDER BY distance_meters ASC
                LIMIT 1;
                """,
                (
                    body.center_lat,        # %s thứ 1
                    body.center_lng,        # %s thứ 2
                    body.center_lat,        # %s thứ 3
                    body.center_lat,        # %s thứ 4
                    body.center_lng,        # %s thứ 5
                    body.center_lat,        # %s thứ 6
                    body.radius_meters      # %s thứ 7
                )
            )

            existing_pin = cur.fetchone()

            # 2. Nếu tìm thấy pin → trả về pin_id có sẵn
            if existing_pin:
                return PinByCoordResponse(
                    pin_id=existing_pin["pin_id"],
                    is_new_pin=False
                )

            # 3. Nếu không tìm thấy → tạo pin mới
            cur.execute(
                """
                INSERT INTO pins (latitude, longitude)
                VALUES (%s, %s)
                RETURNING pin_id;
                """,
                (body.center_lat, body.center_lng)
            )

            new_pin = cur.fetchone()
            connection.commit()

            return PinByCoordResponse(
                pin_id=new_pin["pin_id"],
                is_new_pin=True
            )

    finally:
        connection.close()



# ==================================================
#   TÌM PIN NGẪU NHIÊN TRONG BÁN KÍNH (CHO GAME)
# ==================================================

class FindRandomPinRequest(BaseModel):
    user_lat: float
    user_lng: float
    target_distance: int  # 50, 100, 200, 500 (meters)

class RandomPinResponse(BaseModel):
    pin_id: int
    latitude: float
    longitude: float
    actual_distance: float  # Khoảng cách thực tế đến pin được chọn


@router.post("/find-random")
def find_random_pin(body: FindRandomPinRequest):
    """
    Tìm một pin ngẫu nhiên có khoảng cách gần với target_distance nhất
    trong phạm vi ±50% của target_distance
    """
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            # Tìm tất cả pins trong khoảng target_distance ±50%
            min_distance = body.target_distance * 0.5
            max_distance = body.target_distance * 1.5
            
            # Sử dụng GREATEST để tránh acos > 1 hoặc < -1 gây lỗi
            cur.execute(
                """
                WITH distance_calc AS (
                    SELECT 
                        p.pin_id,
                        p.latitude,
                        p.longitude,
                        (
                            6371000 * acos(
                                LEAST(1.0, GREATEST(-1.0,
                                    cos(radians(%s)) * cos(radians(p.latitude)) *
                                    cos(radians(p.longitude) - radians(%s)) +
                                    sin(radians(%s)) * sin(radians(p.latitude))
                                ))
                            )
                        ) AS distance_meters
                    FROM pins p
                )
                SELECT 
                    pin_id,
                    latitude,
                    longitude,
                    distance_meters
                FROM distance_calc
                WHERE distance_meters BETWEEN %s AND %s
                ORDER BY distance_meters;
                """,
                (
                    body.user_lat, body.user_lng, body.user_lat,
                    min_distance, max_distance
                )
            )
            
            pins = cur.fetchall()
            
            # Log để debug
            print(f"[DEBUG] Target: {body.target_distance}m, Range: {min_distance:.0f}-{max_distance:.0f}m")
            print(f"[DEBUG] Found {len(pins)} pins")
           
            if not pins:
                # Nếu không tìm thấy pin nào trong phạm vi, tìm 1 pin gần nhất
                print("[DEBUG] No pins in range, finding nearest...")
                cur.execute(
                    """
                    WITH distance_calc AS (
                        SELECT 
                            p.pin_id,
                            p.latitude,
                            p.longitude,
                            (
                                6371000 * acos(
                                    LEAST(1.0, GREATEST(-1.0,
                                        cos(radians(%s)) * cos(radians(p.latitude)) *
                                        cos(radians(p.longitude) - radians(%s)) +
                                        sin(radians(%s)) * sin(radians(p.latitude))
                                    ))
                                )
                            ) AS distance_meters
                        FROM pins p
                    )
                    SELECT 
                        pin_id,
                        latitude,
                        longitude,
                        distance_meters
                    FROM distance_calc
                    ORDER BY distance_meters
                    LIMIT 1;
                    """,
                    (body.user_lat, body.user_lng, body.user_lat)
                )
                pins = cur.fetchall()
                
                if pins:
                    print(f"[DEBUG] Nearest pins")
            
            if not pins:
                raise HTTPException(status_code=404, detail="Không tìm thấy pin nào trong hệ thống")
            
            # Chọn ngẫu nhiên một pin từ danh sách
            selected_pin = random.choice(pins)
            
            print(f"[DEBUG] Selected pin {selected_pin['pin_id']} at {selected_pin['distance_meters']:.0f}m")
            
            return RandomPinResponse(
                pin_id=selected_pin["pin_id"],
                latitude=float(selected_pin["latitude"]),
                longitude=float(selected_pin["longitude"]),
                actual_distance=float(selected_pin["distance_meters"])
            )
            
    finally:
        connection.close()