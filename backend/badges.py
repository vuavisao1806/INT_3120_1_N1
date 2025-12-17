from fastapi import APIRouter, HTTPException
from connection import get_database_connection
from pydantic import BaseModel
from typing import List

router = APIRouter(
    prefix="/badges",
    tags=["badges"]
)

# ==================================================
#              LẤY TẤT CẢ HUY HIỆU CỦA USER
# ==================================================

class GetUserBadgesRequest(BaseModel):
    user_id: int

class BadgeSchema(BaseModel):
    badge_id: int
    name: str
    description: str
    icon_name: str
    tier: str
    earned_at: str | None = None
    is_earned: bool

@router.post("/user")
def get_user_badges(body: GetUserBadgesRequest):
    """
    Lấy tất cả huy hiệu và trạng thái của user
    """
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                SELECT 
                    b.badge_id,
                    b.name,
                    b.description,
                    b.icon_name,
                    b.tier,
                    ub.earned_at,
                    CASE WHEN ub.user_id IS NOT NULL THEN true ELSE false END as is_earned
                FROM badges b
                LEFT JOIN user_badges ub ON b.badge_id = ub.badge_id AND ub.user_id = %s
                ORDER BY b.requirement_type, b.requirement_value;
                """,
                (body.user_id,)
            )
            badges = cur.fetchall()
            return badges
    finally:
        connection.close()

# ==================================================
#       LẤY HUY HIỆU ĐÃ ĐẠT ĐƯỢC CỦA USER (ĐỂ HIỂN THỊ)
# ==================================================

class GetEarnedBadgesRequest(BaseModel):
    user_id: int
    limit: int = 3  # Số lượng huy hiệu hiển thị (mặc định 3)

@router.post("/earned")
def get_earned_badges(body: GetEarnedBadgesRequest):
    """
    Lấy các huy hiệu user đã đạt được, sắp xếp theo thời gian mới nhất
    """
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            cur.execute(
                """
                SELECT 
                    b.badge_id,
                    b.name,
                    b.description,
                    b.icon_name,
                    b.tier,
                    ub.earned_at
                FROM user_badges ub
                JOIN badges b ON ub.badge_id = b.badge_id
                WHERE ub.user_id = %s
                ORDER BY ub.earned_at DESC
                LIMIT %s;
                """,
                (body.user_id, body.limit)
            )
            badges = cur.fetchall()
            return badges
    finally:
        connection.close()

# ==================================================
#       KIỂM TRA VÀ CẤP HUY HIỆU CHO USER
# ==================================================

class CheckAndAwardBadgesRequest(BaseModel):
    user_id: int

class NewlyEarnedBadge(BaseModel):
    badge_id: int
    name: str
    description: str
    icon_name: str
    tier: str

class CheckBadgesResponse(BaseModel):
    newly_earned: List[NewlyEarnedBadge]
    total_badges: int

@router.post("/check")
def check_and_award_badges(body: CheckAndAwardBadgesRequest):
    """
    Kiểm tra và tự động cấp huy hiệu cho user dựa trên thành tích
    Trả về danh sách huy hiệu mới đạt được
    """
    connection = get_database_connection()
    newly_earned = []

    try:
        with connection:
            with connection.cursor() as cur:
                # 1. Lấy thống kê của user
                cur.execute(
                    """
                    SELECT 
                        (SELECT COUNT(*) FROM posts WHERE user_id = %s) as total_posts,
                        (SELECT COUNT(*) FROM reactions r
                         JOIN posts p ON r.post_id = p.post_id
                         WHERE p.user_id = %s) as total_reactions,
                        (SELECT COUNT(*) FROM comments WHERE user_id = %s) as total_comments,
                        (SELECT COUNT(*) FROM user_pins WHERE user_id = %s) as total_pins,
                        (SELECT COUNT(*) FROM friends WHERE user_id = %s) as total_friends;
                    """,
                    (body.user_id, body.user_id, body.user_id, body.user_id, body.user_id)
                )
                stats = cur.fetchone()

                if not stats:
                    return CheckBadgesResponse(newly_earned=[], total_badges=0)

                # 2. Lấy tất cả huy hiệu có thể đạt được
                cur.execute(
                    """
                    SELECT badge_id, name, description, icon_name, tier, requirement_type, requirement_value
                    FROM badges
                    WHERE badge_id NOT IN (
                        SELECT badge_id FROM user_badges WHERE user_id = %s
                    )
                    ORDER BY requirement_value;
                    """,
                    (body.user_id,)
                )
                available_badges = cur.fetchall()

                # 3. Kiểm tra từng huy hiệu
                for badge in available_badges:
                    requirement_type = badge['requirement_type']
                    requirement_value = badge['requirement_value']

                    # Map requirement_type với stats
                    stat_mapping = {
                        'posts': stats['total_posts'],
                        'reactions': stats['total_reactions'],
                        'comments': stats['total_comments'],
                        'pins': stats['total_pins'],
                        'friends': stats['total_friends']
                    }

                    user_value = stat_mapping.get(requirement_type, 0)

                    # Nếu đạt yêu cầu, cấp huy hiệu
                    if user_value >= requirement_value:
                        cur.execute(
                            """
                            INSERT INTO user_badges (user_id, badge_id)
                            VALUES (%s, %s)
                            ON CONFLICT DO NOTHING;
                            """,
                            (body.user_id, badge['badge_id'])
                        )

                        if cur.rowcount > 0:
                            newly_earned.append(NewlyEarnedBadge(
                                badge_id=badge['badge_id'],
                                name=badge['name'],
                                description=badge['description'],
                                icon_name=badge['icon_name'],
                                tier=badge['tier']
                            ))

                # 4. Đếm tổng số huy hiệu hiện có
                cur.execute(
                    "SELECT COUNT(*) as total FROM user_badges WHERE user_id = %s;",
                    (body.user_id,)
                )
                total_badges = cur.fetchone()['total']

                return CheckBadgesResponse(
                    newly_earned=newly_earned,
                    total_badges=total_badges
                )

    finally:
        connection.close()

# ==================================================
#       LẤY TIẾN TRÌNH ĐẠT HUY HIỆU
# ==================================================

class GetBadgeProgressRequest(BaseModel):
    user_id: int

class BadgeProgress(BaseModel):
    badge_id: int
    name: str
    description: str
    icon_name: str
    tier: str
    requirement_type: str
    requirement_value: int
    current_value: int
    progress_percentage: float
    is_earned: bool

@router.post("/progress")
def get_badge_progress(body: GetBadgeProgressRequest):
    """
    Lấy tiến trình đạt huy hiệu của user
    """
    connection = get_database_connection()
    try:
        with connection.cursor() as cur:
            # 1. Lấy thống kê
            cur.execute(
                """
                SELECT 
                    (SELECT COUNT(*) FROM posts WHERE user_id = %s) as total_posts,
                    (SELECT COUNT(*) FROM reactions r
                     JOIN posts p ON r.post_id = p.post_id
                     WHERE p.user_id = %s) as total_reactions,
                    (SELECT COUNT(*) FROM comments WHERE user_id = %s) as total_comments,
                    (SELECT COUNT(*) FROM user_pins WHERE user_id = %s) as total_pins,
                    (SELECT COUNT(*) FROM friends WHERE user_id = %s) as total_friends;
                """,
                (body.user_id, body.user_id, body.user_id, body.user_id, body.user_id)
            )
            stats = cur.fetchone()

            # 2. Lấy tất cả huy hiệu
            cur.execute(
                """
                SELECT 
                    b.badge_id,
                    b.name,
                    b.description,
                    b.icon_name,
                    b.tier,
                    b.requirement_type,
                    b.requirement_value,
                    CASE WHEN ub.user_id IS NOT NULL THEN true ELSE false END as is_earned
                FROM badges b
                LEFT JOIN user_badges ub ON b.badge_id = ub.badge_id AND ub.user_id = %s
                ORDER BY b.requirement_type, b.requirement_value;
                """,
                (body.user_id,)
            )
            badges = cur.fetchall()

            # 3. Tính tiến trình
            stat_mapping = {
                'posts': stats['total_posts'],
                'reactions': stats['total_reactions'],
                'comments': stats['total_comments'],
                'pins': stats['total_pins'],
                'friends': stats['total_friends']
            }

            progress_list = []
            for badge in badges:
                current_value = stat_mapping.get(badge['requirement_type'], 0)
                progress_percentage = min(100.0, (current_value / badge['requirement_value']) * 100)

                progress_list.append(BadgeProgress(
                    badge_id=badge['badge_id'],
                    name=badge['name'],
                    description=badge['description'],
                    icon_name=badge['icon_name'],
                    tier=badge['tier'],
                    requirement_type=badge['requirement_type'],
                    requirement_value=badge['requirement_value'],
                    current_value=current_value,
                    progress_percentage=round(progress_percentage, 1),
                    is_earned=badge['is_earned']
                ))

            return progress_list

    finally:
        connection.close()