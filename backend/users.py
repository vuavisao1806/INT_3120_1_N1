from fastapi import APIRouter, HTTPException
from connection import get_database_connection
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
    total_pin: int = 0
    total_reaction: int = 0
    total_comment: int = 0,
    total_contact: int = 0


class LoginResponse(BaseModel):
    success: bool
    user: UserSchema | None = None


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode(), hashed_password.encode())


@router.post("/login", response_model=LoginResponse)
def login(body: LoginRequest):
    connection = get_database_connection()
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

        user.pop("password", None)

        user_id = user["user_id"]

        with connection.cursor() as cur:
            # Query tính toán số liệu
            query_stats = """
                SELECT 
                    -- Đếm số pin người dùng đã lưu
                    (SELECT COUNT(*) FROM user_pins WHERE user_id = %s) AS total_pin,
                    
                    -- Đếm tổng số reaction trên tất cả bài viết của người dùng
                    (
                        SELECT COUNT(*)
                        FROM reactions r
                        JOIN posts p ON r.post_id = p.post_id
                        WHERE p.user_id = %s
                    ) AS total_reaction,
                    
                    -- Đếm tổng số comment trên tất cả bài viết của người dùng
                    (
                        SELECT COUNT(*)
                        FROM comments c
                        JOIN posts p ON c.post_id = p.post_id
                        WHERE p.user_id = %s
                    ) AS total_comment,
                    (
                       SELECT COUNT(*) 
                        FROM request_contact 
                        WHERE followed_user_id = %s 
                        AND status = 'PENDING'
                    ) as total_contact;
            """

            # Thực thi query, truyền user_id vào 3 vị trí %s
            cur.execute(query_stats, (user_id, user_id, user_id, user_id))
            stats = cur.fetchone()

            # Nếu lấy được số liệu, cập nhật vào dictionary user
            if stats:
                user.update(stats)
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
    name: str
    user_password: str
    user_email: str
    avatar_url: str


class RegisterNameInvalid(BaseModel):
    user_name_taken: bool = True


class RegisterEmailInvalid(BaseModel):
    user_email_taken: bool = True


class RegisterSuccess(BaseModel):
    register_success: bool = True


@router.post("/register")
def register(body: RegisterRequest):
    connection = get_database_connection()
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
                INSERT INTO users (user_name, name, password, user_email, avatar_url)
                VALUES (%s, %s, %s, %s, %s);
                """,
                (body.user_name, body.name, hashed_pw,
                 body.user_email, body.avatar_url)
            )

        connection.commit()
        return RegisterSuccess()

    finally:
        connection.close()

# ==================================================
#              THAY ĐỔI THÔNG TIN CÁ NHÂN
# ==================================================


class UpdateUserByUserIdRequest(BaseModel):
    user_id: int
    name: str
    quotes: str
    location: str
    avatar_url: str
    email: str
    website: str


class UpdateUserByUserIdSuccess(BaseModel):
    update_user_by_user_id_success: bool = True


@router.post("/update")
def update(body: UpdateUserByUserIdRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:

            # Check username exists
            cur.execute(
                """
                UPDATE users
                SET
                name=%s,
                quotes = %s ,
                avatar_url = %s,
                location=%s,
                user_email=%s,
                website=%s
                WHERE user_id = %s
                """,
                (body.name, body.quotes, body.avatar_url,
                 body.location, body.email, body.website, body.user_id)
            )

        connection.commit()
        return UpdateUserByUserIdSuccess()

    finally:
        connection.close()


class GetUserByUserIdRequest(BaseModel):
    current_user_id: int
    got_user_id: int


class GetUserByUserIdInvalid(BaseModel):
    get_user_by_user_id_invalid: bool = True


@router.post("/get")
def get(body: GetUserByUserIdRequest):
    connection = get_database_connection()
    try:
        # === BƯỚC 1: LẤY INFO USER ===
        with connection.cursor() as cur:
            cur.execute(
                "SELECT * FROM users WHERE user_id = %s;",
                (body.got_user_id,) # <--- SỬA: Phải dùng got_user_id
            )
            user = cur.fetchone()

        if user is None:
            return GetUserByUserIdInvalid()

        user.pop("password", None)

        # === BƯỚC 2: LẤY SỐ LIỆU (STATS) ===
        with connection.cursor() as cur:
            query_stats = """
                SELECT 
                    (SELECT COUNT(*) FROM user_pins WHERE user_id = %s) AS total_pin,
                    
                    (SELECT COUNT(*) FROM reactions r
                     JOIN posts p ON r.post_id = p.post_id
                     WHERE p.user_id = %s) AS total_reaction,
                    
                    (SELECT COUNT(*) FROM comments c
                     JOIN posts p ON c.post_id = p.post_id
                     WHERE p.user_id = %s) AS total_comment,
                     
                     -- Sửa: Nên đếm số bạn bè (Friends) thay vì đếm Request Pending
                    (SELECT COUNT(*) FROM request_contact WHERE followed_user_id = %s AND status = 'PENDING') as total_contact
            """
            # <--- SỬA: Truyền body.got_user_id vào cả 4 vị trí
            cur.execute(query_stats, (body.got_user_id, body.got_user_id, 
                                      body.got_user_id, body.got_user_id))
            quantity = cur.fetchone()
            if quantity:
                user.update(quantity)

        # === BƯỚC 3: CHECK QUAN HỆ ===
        with connection.cursor() as cur:
            query_relation = """
                SELECT 
                    CASE 
                        WHEN %s = %s THEN 'SELF'
                        WHEN EXISTS (SELECT 1 FROM friends WHERE user_id = %s AND friend_id = %s) THEN 'FRIEND'
                        WHEN EXISTS (SELECT 1 FROM request_contact WHERE following_user_id = %s AND followed_user_id = %s AND status = 'PENDING') THEN 'SENT_REQUEST'
                        WHEN EXISTS (SELECT 1 FROM request_contact WHERE following_user_id = %s AND followed_user_id = %s AND status = 'PENDING') THEN 'INCOMING_REQUEST'
                        ELSE 'STRANGER'
                    END AS relationship_status;
            """
            # Tham số truyền vào phải đúng thứ tự logic CASE WHEN
            params = (
                body.current_user_id, body.got_user_id,      # SELF
                body.current_user_id, body.got_user_id,      # FRIEND
                body.current_user_id, body.got_user_id,      # SENT (Mình gửi)
                body.got_user_id, body.current_user_id       # INCOMING (Họ gửi - lưu ý ngược lại)
            )
            
            cur.execute(query_relation, params)
            stats = cur.fetchone()

            if stats:
                user.update(stats) # Gộp kết quả 'relationship_status' vào user
        
        return user

    finally:
        connection.close()


class CheckIsFriend(BaseModel):
    own_id: int
    other_id: int


class IsFriendRespond(BaseModel):
    is_friend: bool = True


@router.post("/isfriend")
def is_friend(body: CheckIsFriend):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                    SELECT 1 FROM friends
                    WHERE user_id = %s AND friend_id = %s;
                """,
                (body.own_id, body.other_id,)
            )
            status: IsFriendRespond = IsFriendRespond()
            if (cur.rowcount == 0):
                status.is_friend = False
            return status

    finally:
        connection.close()


class showContactRequest(BaseModel):
    user_id: int


@router.post("/contact_request")
def showContactList(body: showContactRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                    SELECT 
                    u.user_id, 
                    u.user_name, 
                    u.avatar_url,
                    r.status, 
                    r.created_at 
                FROM request_contact r
                JOIN users u ON r.following_user_id = u.user_id
                WHERE r.followed_user_id = %s
                AND r.status = 'PENDING'
                ORDER BY r.created_at DESC;
                    """,
                (body.user_id,)
            )
            contacts = cur.fetchall()
            return contacts

    finally:
        connection.close()


class RespondContactRequest(BaseModel):
    own_id: int
    other_id: int
    isAccept: bool


class IsSuccessRespond(BaseModel):
    is_success: bool = True


@router.post("/respond_contact")
def respondContact(body: RespondContactRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            new_status = 'CANCELED'
            if body.isAccept:
                new_status = 'ACCEPTED'

            cur.execute(
                """
                UPDATE request_contact
                SET status = %s
                WHERE followed_user_id = %s 
                AND following_user_id = %s    
                AND status = 'PENDING';
                """,

                (
                    new_status,
                    body.own_id,
                    body.other_id
                )
            )

            if cur.rowcount == 0:
                return IsSuccessRespond(is_success=False)

            if body.isAccept:
                cur.execute(
                    """
                INSERT INTO friends (user_id, friend_id)
                VALUES (%s, %s), (%s, %s)
                """,
                    (body.own_id, body.other_id, body.other_id, body.own_id,)
                )
            connection.commit()
            return IsSuccessRespond(is_success=True)

    except Exception as e:
        connection.rollback()
        print(f"Error: {e}")
        return IsSuccessRespond(is_success=False)
    finally:
        connection.close()


class SendContactRequestSchema(BaseModel):
    following_user_id: int
    followed_user_id: int
    message: str = ""


class SendContactResult(BaseModel):
    is_success: bool


@router.post("/send_contact")
def sendContact(body: SendContactRequestSchema):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                INSERT INTO request_contact 
                (following_user_id, followed_user_id, message, status, created_at)
                VALUES (%s, %s, %s, 'PENDING', NOW())
                RETURNING request_id;
                    """,
                (body.following_user_id, body.followed_user_id, body.message)
            )
            if cur.rowcount == 0:
                return SendContactResult(is_success=False)
        connection.commit()
        return SendContactResult(is_success=True)
    finally:
        connection.close()
      
class UserFavoriteTagsRequest(BaseModel):
    user_id: int
    number_tags: int

@router.post("/tags")
def get_favorite_tags_by_user_id(body: UserFavoriteTagsRequest):
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                SELECT ut.tag_id, t.name FROM users_tags AS ut
                INNER JOIN tags AS t ON ut.tag_id = t.tag_id
                WHERE user_id = %s
                ORDER BY ut.cnt DESC
                LIMIT %s;
                """,
                (body.user_id, body.number_tags)
            )
            tags = cur.fetchall()
            return tags
    finally:
        connection.close()
