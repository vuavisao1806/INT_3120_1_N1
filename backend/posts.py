from fastapi import APIRouter
from connection import get_connection
from pydantic import BaseModel
from psycopg2.extras import RealDictCursor

router = APIRouter(
    prefix="/posts",
    tags=["posts"]
)

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

@router.post("/insert")
def register(body: InsertPostRequest):
    connection = get_connection()
    try:
        with connection.cursor() as cur:
            # Check username exists
            cur.execute(
                """
                INSERT INTO posts (pin_id, user_id, title, body, image_url, status)
                VALUES (%s, %s,%s,%s,%s,%s)
                """,
                (body.pin_id, body.user_id, body.title, body.body, body.image_url, body.status)
            )
            
        connection.commit()
        return InsertPostSuccess()

    finally:
        connection.close()
        
        
# ==================================================
#       LẤY DANH SÁCH GHIM TRONG VÙNG BÁN KÍNH
# ==================================================  

class GetPinsInRadiusRequest(BaseModel):
    center_lat: float
    center_lng: float
    radius_meters: float


@router.post("/pins/get/in-radius")
def get_pins_in_radius(body: GetPinsInRadiusRequest):
    connection = get_connection()
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
#       Tương tác comment với bài viết
# ==================================================  

class CreateCommentRequest(BaseModel):
    post_id: int
    user_id: int
    content: str

@router.post("/comment")
def create_comment(body: CreateCommentRequest):
    connection = get_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Insert comment
                cur.execute(
                    """
                    INSERT INTO comments (post_id, user_id, content)
                    VALUES (%s, %s, %s)
                    RETURNING comment_id, created_at;
                    """,
                    (body.post_id, body.user_id, body.content)
                )
                comment = cur.fetchone()

                # 2. Update comment_count
                cur.execute(
                    """
                    UPDATE posts
                    SET comment_count = comment_count + 1
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Update users_tags for all tags of that post
                cur.execute(
                    """
                    INSERT INTO users_tags (user_id, tag_id, cnt)
                    SELECT %s AS user_id, pt.tag_id, 1
                    FROM post_tags pt
                    WHERE pt.post_id = %s
                    ON CONFLICT (user_id, tag_id)
                    DO UPDATE SET cnt = users_tags.cnt + 1;
                    """,
                    (body.user_id, body.post_id)
                )
    finally:
        connection.close()

# ==================================================
#       Tương tác thả tim với bài viết
# ================================================== 

class ReactionRequest(BaseModel):
    post_id: int
    user_id: int

@router.post("/react")
def react_post(body: ReactionRequest):
    connection = get_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Insert reaction nếu chưa có
                cur.execute(
                    """
                    INSERT INTO reactions (post_id, user_id)
                    VALUES (%s, %s)
                    ON CONFLICT (post_id, user_id) DO NOTHING;
                    """,
                    (body.post_id, body.user_id)
                )

                # Nếu không insert được (đã tim rồi) thì không cần cộng nữa
                if cur.rowcount == 0:
                    return {"status": "already_reacted"}

                # 2. Tăng reaction_count
                cur.execute(
                    """
                    UPDATE posts
                    SET reaction_count = reaction_count + 1
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Update users_tags cho tất cả tag của post
                cur.execute(
                    """
                    INSERT INTO users_tags (user_id, tag_id, cnt)
                    SELECT %s AS user_id, pt.tag_id, 1
                    FROM post_tags pt
                    WHERE pt.post_id = %s
                    ON CONFLICT (user_id, tag_id)
                    DO UPDATE SET cnt = users_tags.cnt + 1;
                    """,
                    (body.user_id, body.post_id)
                )
    finally:
        connection.close()
# ==================================================
#       Tương tác hủy thả tim với bài viết
# ================================================== 

class CancelReactionRequest(BaseModel):
    post_id: int
    user_id: int
@router.post("/react/cancel")
def cancel_react_post(body: CancelReactionRequest):
    connection = get_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Xóa reaction (nếu có)
                cur.execute(
                    """
                    DELETE FROM reactions
                    WHERE post_id = %s AND user_id = %s;
                    """,
                    (body.post_id, body.user_id)
                )

                # Nếu không xóa được dòng nào thì thôi, không làm gì thêm
                if cur.rowcount == 0:
                    return

                # 2. Giảm reaction_count của post (nếu có cột này)
                cur.execute(
                    """
                    UPDATE posts
                    SET reaction_count = GREATEST(reaction_count - 1, 0)
                    WHERE post_id = %s;
                    """,
                    (body.post_id,)
                )

                # 3. Giảm cnt trong users_tags cho các tag của post đó
                #    (1 lần hủy tim => trừ 1 điểm cho mỗi tag)
                cur.execute(
                    """
                    UPDATE users_tags ut
                    SET cnt = GREATEST(ut.cnt - 1, 0)
                    FROM post_tags pt
                    WHERE ut.user_id = %s
                      AND ut.tag_id = pt.tag_id
                      AND pt.post_id = %s;
                    """,
                    (body.user_id, body.post_id)
                )

                # (optional) Nếu muốn xóa luôn những dòng cnt <= 0:
                # cur.execute(
                #     "DELETE FROM users_tags WHERE user_id = %s AND cnt <= 0;",
                #     (body.user_id,)
                # )

        # tạm thời không trả về gì
        return
    finally:
        connection.close()


# ==================================================
#       Tương tác hủy comment với bài viết
# ==================================================  

class CancelCommentRequest(BaseModel):
    comment_id: int
    user_id: int
@router.post("/comment/cancel")
def cancel_comment(body: CancelCommentRequest):
    connection = get_connection()
    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Lấy post_id của comment để còn update posts + users_tags
                cur.execute(
                    """
                    SELECT post_id
                    FROM comments
                    WHERE comment_id = %s AND user_id = %s;
                    """,
                    (body.comment_id, body.user_id)
                )
                row = cur.fetchone()

                # Không tìm thấy comment (hoặc không thuộc user này) thì thôi
                if row is None:
                    return

                post_id = row[0]

                # 2. Xóa comment
                cur.execute(
                    """
                    DELETE FROM comments
                    WHERE comment_id = %s AND user_id = %s;
                    """,
                    (body.comment_id, body.user_id)
                )

                if cur.rowcount == 0:
                    # Về lý thuyết không xảy ra vì vừa SELECT, nhưng cứ check cho chắc
                    return

                # 3. Giảm comment_count của post (nếu có cột này)
                cur.execute(
                    """
                    UPDATE posts
                    SET comment_count = GREATEST(comment_count - 1, 0)
                    WHERE post_id = %s;
                    """,
                    (post_id,)
                )

                # 4. Giảm cnt trong users_tags cho các tag của post đó
                #    (mỗi lần hủy 1 comment => trừ 1 điểm cho mỗi tag)
                cur.execute(
                    """
                    UPDATE users_tags ut
                    SET cnt = GREATEST(ut.cnt - 1, 0)
                    FROM post_tags pt
                    WHERE ut.user_id = %s
                      AND ut.tag_id = pt.tag_id
                      AND pt.post_id = %s;
                    """,
                    (body.user_id, post_id)
                )

                # (optional) Xóa dòng users_tags nếu cnt <= 0
                # cur.execute(
                #     "DELETE FROM users_tags WHERE user_id = %s AND cnt <= 0;",
                #     (body.user_id,)
                # )

        # tạm thời không trả về gì
        return
    finally:
        connection.close()


# ==================================================
#       Lấy 1 post theo post_id 
# ================================================== 

class GetPostRequest(BaseModel):
    post_id: int

@router.post("/get")
def get_post(body: GetPostRequest):
    connection = get_connection()
    try:
        # dùng RealDictCursor để trả về dạng dict -> FastAPI tự convert sang JSON đẹp
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT 
                    p.*, 
                    u.user_name,
                    u.avatar_url
                FROM posts p
                JOIN users u ON u.user_id = p.user_id
                WHERE p.post_id = %s;
                """,
                (body.post_id,)
            )
            row = cur.fetchone()
            return row
    finally:
        connection.close()

# ==================================================
#       Lấy danh sách comment của 1 bài viết (JOIN users)
# ==================================================
class GetPostCommentsRequest(BaseModel):
    post_id: int

@router.post("/get/comments")
def get_comments_of_post(body: GetPostCommentsRequest):
    connection = get_connection()
    try:
        # dùng RealDictCursor để trả về dict thay vì tuple
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT 
                    c.comment_id,
                    c.post_id,
                    c.user_id,
                    c.content,
                    c.created_at,
                    u.user_name,      
                    u.avatar_url   
                FROM comments c
                JOIN users u ON u.user_id = c.user_id
                WHERE c.post_id = %s
                ORDER BY c.created_at ASC;
                """,
                (body.post_id,)
            )
            comments = cur.fetchall()
            return comments
    finally:
        connection.close()


class GetPostTagsRequest(BaseModel):
    post_id: int

@router.post("/get/tags")
def get_post_tags(body: GetPostTagsRequest):
    connection = get_connection()
    try:
        with connection.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(
                """
                SELECT t.tag_id, t.name
                FROM post_tags pt
                JOIN tags t ON t.tag_id = pt.tag_id
                WHERE pt.post_id = %s
                ORDER BY t.name;
                """,
                (body.post_id,)
            )
            return cur.fetchall()
    finally:
        connection.close()
