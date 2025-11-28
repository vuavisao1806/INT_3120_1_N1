from fastapi import FastAPI
import psycopg2
from psycopg2.extras import RealDictCursor
import os
from dotenv import load_dotenv
from pydantic import BaseModel
import bcrypt

load_dotenv()
app = FastAPI()


def get_connection():
    return psycopg2.connect(
        host=os.getenv("PG_HOST"),
        port=os.getenv("PG_PORT"),
        dbname=os.getenv("PG_DB"),
        user=os.getenv("PG_USER"),
        password=os.getenv("PG_PASSWORD"),
        sslmode=os.getenv("PG_SSLMODE", "require"),
        cursor_factory=RealDictCursor
    )


# ====================================
#              ĐĂNG NHẬP
# ====================================

class LoginRequest(BaseModel):
    user_name: str
    user_password: str

class LoginInvalid(BaseModel):
    invalid_user: bool = True


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode(), hashed_password.encode())


@app.post("/users/login")
def login(body: LoginRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT * FROM users WHERE user_name = %s;",
                (body.user_name,)
            )
            user = cur.fetchone()

        if user is None:
            return LoginInvalid()

        if not verify_password(body.user_password, user["password"]):
            return LoginInvalid()

        # Xóa password khi trả về
        user.pop("password", None)

        return user

    finally:
        conn.close()



# ====================================
#              ĐĂNG KÍ
# ====================================

class RegisterRequest(BaseModel):
    user_name: str
    user_password: str
    user_email: str


class RegisterNameInvalid(BaseModel):
    user_name_taken: bool = True

class RegisterEmailInvalid(BaseModel):
    user_email_taken: bool = True

class RegisterSuccess(BaseModel):
    register_success: bool = True


@app.post("/users/register")
def register(body: RegisterRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:

            # Check username exists
            cur.execute(
                "SELECT 1 FROM users WHERE user_name = %s;",
                (body.user_name,)
            )
            if cur.fetchone():
                return RegisterNameInvalid()

            # Check email exists
            cur.execute(
                "SELECT 1 FROM users WHERE user_email = %s;",
                (body.user_email,)
            )
            if cur.fetchone():
                return RegisterEmailInvalid()

            # Hash password
            hashed_pw = hash_password(body.user_password)

            # Insert new user
            cur.execute(
                """
                INSERT INTO users (user_name, password, user_email)
                VALUES (%s, %s, %s);
                """,
                (body.user_name, hashed_pw, body.user_email)
            )

        conn.commit()
        return RegisterSuccess()

    finally:
        conn.close()


# ==================================================
#              THAY ĐỔI THÔNG TIN CÁ NHÂN
# ==================================================

class UpdateUserByUserIdRequest(BaseModel):
    user_id: str
    quotes: str
    avatar_url: str
    contact_info: str
    
class UpdateUserByUserIdSuccess(BaseModel):
    update_user_by_user_id_success: bool = True

@app.post("/users/update")
def register(body: UpdateUserByUserIdRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:

            # Check username exists
            cur.execute(
                """
                UPDATE users
                SET
                    quotes = %s ,
                    avatar_url = %s,
                    contact_info = %s
                WHERE user_id = %s
                """,
                (body.quotes,body.avatar_url, body.contact_info, body.user_id)
            )
            
        conn.commit()
        return UpdateUserByUserIdSuccess()

    finally:
        conn.close()
        
class GetUserByUserIdRequest(BaseModel):
    user_id: str
class GetUserByUserIdInvalid(BaseModel):
    get_user_by_user_id_invalid: bool = True

@app.post("/users/get")
def login(body: GetUserByUserIdRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT * FROM users WHERE user_id = %s;",
                (body.user_id,)
            )
            user = cur.fetchone()

        if user is None:
            return GetUserByUserIdInvalid()

        # Xóa password khi trả về
        user.pop("password", None)

        return user

    finally:
        conn.close()


# ==================================================
#              TẠO MỘT GHIM MỚI
# ==================================================


class InsertPinRequest(BaseModel):
    latitude: float
    longitude: float
    
class InsertPinSuccess(BaseModel):
    insert_pin_success: bool = True

@app.post("/pins/insert")
def register(body: InsertPinRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            # Check username exists
            cur.execute(
                """
                INSERT INTO pins (latitude, longitude)
                VALUES (%s, %s)
                """,
                (body.latitude,body.longitude)
            )
            
        conn.commit()
        return InsertPinSuccess()

    finally:
        conn.close()
        
# ==================================================
#              LẤY DANH SÁCH GHIM THEO USER_ID
# ==================================================

class GetPinListByUserIdRequest(BaseModel):
    user_id: int

@app.post("/pins/get/user-id")
def get_pins_by_user_id(body: GetPinListByUserIdRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
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
        conn.close()

        
# ==================================================
#              TẠO MỘT POST
# ==================================================


class InsertPostRequest(BaseModel):
    pin_id: int
    user_id: int
    title: str
    body: str
    image_url: str
    status: str
    
    
class InsertPostSuccess(BaseModel):
    insert_post_success: bool = True

@app.post("/posts/insert")
def register(body: InsertPostRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            # Check username exists
            cur.execute(
                """
                INSERT INTO posts (pin_id, user_id, title, body, image_url, status)
                VALUES (%s, %s,%s,%s,%s,%s)
                """,
                (body.pin_id, body.user_id, body.title, body.body, body.image_url, body.status)
            )
            
        conn.commit()
        return InsertPostSuccess()

    finally:
        conn.close()
        
        
# ==================================================
#       LẤY DANH SÁCH GHIM TRONG VÙNG BÁN KÍNH
# ==================================================  

class GetPinsInRadiusRequest(BaseModel):
    user_id: int
    center_lat: float
    center_lng: float
    radius_meters: float


@app.post("/pins/get/in-radius")
def get_pins_in_radius(body: GetPinsInRadiusRequest):
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            # Haversine: tính khoảng cách giữa (center_lat, center_lng) và (p.latitude, p.longitude)
            # 6371000: bán kính Trái Đất ~ 6,371km (đơn vị mét)
            cur.execute(
                """
                SELECT p.*
                FROM pins p
                INNER JOIN user_pins up ON p.pin_id = up.pin_id
                WHERE up.user_id = %s
                  AND (
                    6371000 * acos(
                        cos(radians(%s)) * cos(radians(p.latitude::double precision)) *
                        cos(radians(p.longitude::double precision) - radians(%s)) +
                        sin(radians(%s)) * sin(radians(p.latitude::double precision))
                    )
                  ) <= %s;
                """,
                (
                    body.user_id,
                    body.center_lat,        # %s thứ 2: lat tâm
                    body.center_lng,        # %s thứ 3: lng tâm
                    body.center_lat,        # %s thứ 4: lat tâm
                    body.radius_meters      # %s thứ 5: bán kính (m)
                )
            )

            pins = cur.fetchall()
            return pins
    finally:
        conn.close()



