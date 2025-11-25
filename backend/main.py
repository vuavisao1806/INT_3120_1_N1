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


@app.post("/login")
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


@app.post("/register")
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
