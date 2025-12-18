package com.example.locationpins.ui.component

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.BadgeProgress
import kotlinx.coroutines.launch

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
            containerColor =  Color.White
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bạn đã đạt được huy hiệu",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = badgeName,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = tierColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badgeDescription,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
        ,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tuyệt vời!")
            }
        }
    )
}

/**
 * Dialog hiển thị chi tiết badges với 2 chế độ:
 * 1. Chi tiết: Swipe để xem từng badge đã đạt được
 * 2. Tiến trình: Scroll để xem tất cả badges với thanh progress
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BadgeDetailDialog(
    earnedBadges: List<Badge>,
    allProgress: List<BadgeProgress>,
    initialBadgeIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var showProgressMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header với nút đóng và switch mode
                BadgeDialogHeader(
                    showProgressMode = showProgressMode,
                    onToggleMode = { showProgressMode = !showProgressMode },
                    onDismiss = onDismiss
                )

                // Content: Chi tiết hoặc Tiến trình
                if (showProgressMode) {
                    ProgressModeContent(allProgress = allProgress)
                } else {
                    DetailModeContent(
                        earnedBadges = earnedBadges,
                        initialIndex = initialBadgeIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeDialogHeader(
    showProgressMode: Boolean,
    onToggleMode: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (showProgressMode) "Tiến trình huy hiệu" else "Huy hiệu của bạn",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Toggle button
            IconButton(
                onClick = onToggleMode,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (showProgressMode) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (showProgressMode) Icons.Default.Star else Icons.Default.BarChart,
                    contentDescription = if (showProgressMode) "Xem chi tiết" else "Xem tiến trình",
                    tint = if (showProgressMode) Color(0xFF1976D2) else Color(0xFFFF9800),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailModeContent(
    earnedBadges: List<Badge>,
    initialIndex: Int
) {
    if (earnedBadges.isEmpty()) {
        EmptyBadgeState()
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, earnedBadges.lastIndex),
        pageCount = { earnedBadges.size }
    )
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / ${earnedBadges.size}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Swipeable badges
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            BadgeDetailPage(badge = earnedBadges[page])
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = pagerState.currentPage > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Trước",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Trước")
            }

            Spacer(Modifier.width(16.dp))

            // Next button
            Button(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < earnedBadges.lastIndex) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = pagerState.currentPage < earnedBadges.lastIndex,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Tiếp")
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Tiếp",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BadgeDetailPage(badge: Badge) {
    val tierColor = getColorByTier(badge.tier)
    val icon = getIconFromName(badge.iconName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Badge icon (lớn)
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(tierColor.copy(alpha = 0.15f))
                .border(4.dp, tierColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tierColor,
                modifier = Modifier.size(90.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Tier badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = tierColor.copy(alpha = 0.2f),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = badge.tier.uppercase(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = tierColor
            )
        }

        // Badge name
        Text(
            text = badge.name,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text(
            text = badge.description,
            fontSize = 16.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        badge.earnedAt?.let { date ->
            Spacer(Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = tierColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Đạt được: $date",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ProgressModeContent(allProgress: List<BadgeProgress>) {
    // Sắp xếp theo % hoàn thành (cao → thấp)
    val sortedProgress = remember(allProgress) {
        allProgress.sortedByDescending { it.progressPercentage }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sortedProgress) { progress ->
            BadgeProgressCard(progress = progress)
        }

        // Bottom spacing
        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmptyBadgeState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Chưa có huy hiệu nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Hãy tạo bài viết, tương tác để nhận huy hiệu đầu tiên!",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}


@Preview
@Composable
fun NewBadgeDialogPreview(){
    NewBadgeDialog(
        badgeName = "Nhà thám hiểm",
        badgeDescription = "Tạo 50 ghim",
        badgeIcon = "Explore",
        badgeTier = "Bronze",
        onDismiss = {}
    )
}