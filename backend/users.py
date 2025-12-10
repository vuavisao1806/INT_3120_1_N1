from fastapi import APIRouter
from connection import get_connection
from pydantic import BaseModel

import bcrypt

router = APIRouter(
    prefix="/users",
    tags=["users"]
)

# ====================================
#              ĐĂNG NHẬP
# ====================================


class LoginRequest(BaseModel):
    user_name: str
    user_password: str


class UserSchema(BaseModel):
    user_id: int
    user_name: str
    user_email: str | None = None
    avatar_url: str | None = None
    quotes: str | None = None
    location: str | None = None
    name: str | None = None
    phone_num: str | None = None
    website: str | None = None




class LoginResponse(BaseModel):
    success: bool
    user: UserSchema | None = None


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode(), hashed_password.encode())


@router.post("/login", response_model=LoginResponse)
def login(body: LoginRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                "SELECT * FROM users WHERE user_name = %s;",
                (body.user_name,)
            )
            user = cur.fetchone()

        if user is None:
            return LoginResponse(
                success=False,
                user=None
            )

        if not verify_password(body.user_password, user["password"]):
            return LoginResponse(
                success=False,
                user=None
            )

        # --- Trường hợp Thành công ---
        user.pop("password", None)

        return LoginResponse(
            success=True,
            user=user
        )

    finally:
        connection.close()


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


@router.post("/register")
def register(body: RegisterRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:

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

        connection.commit()
        return RegisterSuccess()

    finally:
        connection.close()

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


@router.post("/update")
def update(body: UpdateUserByUserIdRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:

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
                (body.quotes, body.avatar_url, body.contact_info, body.user_id)
            )

        connection.commit()
        return UpdateUserByUserIdSuccess()

    finally:
        connection.close()


class GetUserByUserIdRequest(BaseModel):
    user_id: str


class GetUserByUserIdInvalid(BaseModel):
    get_user_by_user_id_invalid: bool = True


@router.post("/get")
def get(body: GetUserByUserIdRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:
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
        connection.close()
