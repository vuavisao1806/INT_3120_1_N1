package com.example.locationpins.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.BadgeProgress

/**
 * Chuyển đổi tên icon từ string sang ImageVector
 */
fun getIconFromName(iconName: String): ImageVector {
    return when (iconName) {
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "Create" -> Icons.Default.Create
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "Star" -> Icons.Default.Star
        "Whatshot" -> Icons.Default.Whatshot
        "FavoriteBorder" -> Icons.Default.FavoriteBorder
        "Grade" -> Icons.Default.Grade
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "ChatBubbleOutline" -> Icons.Default.ChatBubbleOutline
        "Forum" -> Icons.Default.Forum
        "QuestionAnswer" -> Icons.Default.QuestionAnswer
        "Explore" -> Icons.Default.Explore
        "Terrain" -> Icons.Default.Terrain
        "Public" -> Icons.Default.Public
        "Group" -> Icons.Default.Group
        "Groups" -> Icons.Default.Groups
        "Diversity3" -> Icons.Default.Diversity3
        else -> Icons.Default.EmojiEvents
    }
}

/**
 * Lấy màu theo tier
 */
fun getColorByTier(tier: String): Color {
    return when (tier.lowercase()) {
        "bronze" -> Color(0xFFCD7F32)
        "silver" -> Color(0xFFC0C0C0)
        "gold" -> Color(0xFFFFD700)
        "platinum" -> Color(0xFFE5E4E2)
        else -> Color.Gray
    }
}

/**
 * Component hiển thị 1 huy hiệu nhỏ (dùng trong profile, bên cạnh tên user)
 */
@Composable
fun BadgeIcon(
    badge: Badge,
    size: Int = 28,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val icon = getIconFromName(badge.iconName)
    val tierColor = getColorByTier(badge.tier)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(tierColor.copy(alpha = 0.2f))
            .border(1.5.dp, tierColor, CircleShape)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = badge.name,
            tint = tierColor,
            modifier = Modifier.size((size * 0.6).dp)
        )
    }
}

/**
 * Hiển thị danh sách huy hiệu ngang (LazyRow)
 */
@Composable
fun BadgeRow(
    badges: List<Badge>,
    modifier: Modifier = Modifier,
    onBadgeClick: (Badge) -> Unit = {}
) {
    if (badges.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(badges) { badge ->
            BadgeIcon(
                badge = badge,
                size = 32,
                onClick = { onBadgeClick(badge) }
            )
            Log.d("Badge",badge.description.toString())
        }
    }
}

/**
 * Card hiển thị chi tiết huy hiệu (dùng trong dialog hoặc bottom sheet)
 */
@Composable
fun BadgeDetailCard(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    val icon = getIconFromName(badge.iconName)
    val tierColor = getColorByTier(badge.tier)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isEarned) tierColor.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(tierColor.copy(alpha = 0.2f))
                    .border(2.dp, tierColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tierColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (badge.isEarned) Color.Black else Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    fontSize = 13.sp,
                    color = if (badge.isEarned) Color.DarkGray else Color.Gray,
                    lineHeight = 18.sp
                )

                if (badge.isEarned && badge.earnedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đạt được: ${badge.earnedAt}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // Status
            if (badge.isEarned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Đã đạt được",
                    tint = tierColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Card hiển thị tiến trình đạt huy hiệu
 */
@Composable
fun BadgeProgressCard(
    progress: BadgeProgress,
    modifier: Modifier = Modifier
) {
    val icon = getIconFromName(progress.iconName)
    val tierColor = getColorByTier(progress.tier)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isEarned) tierColor.copy(alpha = 0.1f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tierColor.copy(alpha = 0.2f))
                        .border(2.dp, tierColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progress.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = progress.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Status
                if (progress.isEarned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Đã đạt được",
                        tint = tierColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${progress.currentValue}/${progress.requirementValue}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${progress.progressPercentage.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = tierColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progress.progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = tierColor,
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

/**
 * Dialog hiển thị huy hiệu mới đạt được
 */
@Composable
fun NewBadgeDialog(
    badgeName: String,
    badgeDescription: String,
    badgeIcon: String,
    badgeTier: String,
    onDismiss: () -> Unit
) {
    val icon = getIconFromName(badgeIcon)
    val tierColor = getColorByTier(badgeTier)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(tierColor.copy(alpha = 0.2f))
                    .border(3.dp, tierColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tierColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = {
            Text(
                text = "Chúc mừng! 🎉",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bạn đã đạt được huy hiệu",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = badgeName,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = tierColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badgeDescription,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tuyệt vời!")
            }
        }
    )
}