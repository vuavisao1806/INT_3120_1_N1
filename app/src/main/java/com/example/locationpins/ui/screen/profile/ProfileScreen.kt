package com.example.locationpins.ui.screen.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.locationpins.R
import com.example.locationpins.data.model.Badge
import com.example.locationpins.data.model.User
import com.example.locationpins.ui.component.BadgeDetailDialog
import com.example.locationpins.ui.component.BadgeRow
import com.example.locationpins.ui.component.NewBadgeDialog
import com.example.locationpins.ui.screen.gallery.PostGridItemWithStats
import com.example.locationpins.ui.screen.gallery.PostSummary
import com.example.locationpins.ui.screen.login.CurrentUser

@Composable
fun ProfileScreen(
    userId: Int,
    onEditClick: () -> Unit,
    onProfileClick: (Int) -> Unit,
    onPressPost: (PostSummary) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.setUser(userId)
        viewModel.loadPostsForSelf(CurrentUser.currentUser!!.userId)

        if (userId == CurrentUser.currentUser?.userId) {
            viewModel.checkNewBadges()
        }
    }

    val user = uiState.user
    val profileMode = uiState.profileMode

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            val badges = uiState.badges
            when (profileMode) {
                is ProfileMode.Self -> ProfileSelfView(
                    user = user,
                    onInvitesClick = { viewModel.onShowContactRequests() },
                    onEditClick = onEditClick,
                    badges = badges,
                    onBadgeClick = { badge ->
                        val index = badges.indexOf(badge)
                        viewModel.showBadgeDetailDialog(index)
                    },
                    onPressPost = onPressPost,
                    currentPosts = uiState.currentPosts
                )

                is ProfileMode.Friend -> ProfileFriendView(
                    user = user,
                    badges = badges,
                    onBadgeClick = { badge ->
                        val index = badges.indexOf(badge)
                        viewModel.showBadgeDetailDialog(index)
                    }
                )

                ProfileMode.Stranger -> ProfileStrangerView(
                    user = user,
                    onGetContactClick = { viewModel.onGetContactClick() },
                    onAcceptClick = { viewModel.onAcceptContact() },
                    onRejectClick = { viewModel.onRejectContact() },
                    badges = badges,
                    onBadgeClick = { badge ->
                        val index = badges.indexOf(badge)
                        viewModel.showBadgeDetailDialog(index)
                    }
                )
            }

            // Dialog form nhập yêu cầu liên hệ
            if (uiState.showRequestContact) {
                RequestContactScreen(
                    onDismiss = { viewModel.onDismissRequestDialog() },
                    onSend = { viewModel.onSendClick() },
                    onMessageChange = { viewModel.onMessageChange(it) },
                    message = viewModel.getMessage()
                )
            }

            // BottomSheet danh sách yêu cầu kết bạn
            if (uiState.showContactRequests) {
                ContactRequestsSheet(
                    requests = uiState.pendingRequests,
                    onDismiss = { viewModel.onDismissContactRequests() },
                    onAccept = { viewModel.onAcceptContact(it) },
                    onReject = { viewModel.onRejectContact(it) },
                    onShowProfileClick = onProfileClick
                )
            }

            // Dialog chúc mừng badge mới
            if (uiState.showNewBadgeDialog && uiState.selectedNewBadge != null) {
                NewBadgeDialog(
                    badgeName = uiState.selectedNewBadge!!.name,
                    badgeDescription = uiState.selectedNewBadge!!.description,
                    badgeIcon = uiState.selectedNewBadge!!.iconName,
                    badgeTier = uiState.selectedNewBadge!!.tier,
                    onDismiss = { viewModel.dismissNewBadgeDialog() }
                )
            }

            // Dialog chi tiết badges (swipe + progress mode)
            if (uiState.showBadgeDetailDialog) {
                BadgeDetailDialog(
                    earnedBadges = uiState.allEarnedBadges,
                    allProgress = uiState.allBadgeProgress,
                    initialBadgeIndex = uiState.selectedBadgeIndex,
                    onDismiss = { viewModel.dismissBadgeDetailDialog() }
                )
            }
        }
    }
}


@Composable
fun ProfileFriendView(
    user: User?,
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarAndNameColumn(user = user, badges = badges, onBadgeClick = onBadgeClick)
        InfoUserRow(user)
        ParametersRow(user)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileSelfView(
    user: User?,
    onInvitesClick: () -> Unit,
    onEditClick: () -> Unit,
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    onPressPost: (PostSummary) -> Unit,
    currentPosts: List<PostSummary>,
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.background
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        SelfActionRow(
                            pendingInvites = user?.quantityContact ?: 0,
                            onInvitesClick = onInvitesClick,
                            onEditClick = onEditClick
                        )
                    }
                }
                AvatarAndNameColumn(user = user, badges = badges, onBadgeClick = onBadgeClick)
                InfoUserRow(user)
                ParametersRow(user)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (currentPosts.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có bài viết nào",
                        color = Color.Gray
                    )
                }
            }
        } else {
            items(currentPosts) { post ->
                PostGridItemWithStats(
                    post = post,
                    onClick = { onPressPost(post) }
                )
            }
        }
    }
}

@Composable
fun ProfileStrangerView(
    user: User?,
    onGetContactClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarAndNameColumn(user = user, badges = badges, onBadgeClick = onBadgeClick)
        when (user!!.status) {
            "STRANGER" -> GetContactButton(onClick = onGetContactClick)
            "SENT_REQUEST" -> GetSentContact()
            "INCOMING_REQUEST" -> GetRespondRequestButton(
                onAcceptClick = onAcceptClick,
                onRejectClick = onRejectClick
            )
        }
        ParametersRow(user)
    }
}

@Composable
fun AvatarAndNameColumn(
    user: User?,
    badges: List<Badge> = emptyList(),
    onBadgeClick: (Badge) -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user?.avatarUrl)
                .crossfade(true)
                .build(),
            error = painterResource(R.drawable.empty_avatar),
            placeholder = painterResource(R.drawable.empty_avatar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(top = 16.dp)
                .height(120.dp)
                .width(120.dp)
                .border(2.dp, Color.LightGray, RoundedCornerShape(60.dp))
                .clip(RoundedCornerShape(60.dp))
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = user?.name ?: "User",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${user?.userName}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        if (badges.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            BadgeRow(
                badges = badges.take(5),
                onBadgeClick = onBadgeClick
            )
        }

        user?.quote?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4C4C4C)
            )
        }
    }
}

@Composable
fun InfoUserRow(user: User?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.location?.let { InfoLine("📍", it) }
        InfoLine("📧", user?.userEmail ?: "email")
        user?.phoneNumber?.let { InfoLine("📱", it) }
        user?.website?.let { InfoLine("🌐", it) }
    }
}

@Composable
fun InfoLine(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(text = icon, modifier = Modifier.padding(end = 4.dp))
        Text(text)
    }
}

@Composable
fun GetContactButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1665D8),
            contentColor = Color.White
        )
    ) {
        Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Get contact")
    }
}

@Composable
fun GetRespondRequestButton(
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAcceptClick,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1665D8),
                contentColor = Color.White
            )
        ) {
            Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Đồng ý", fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = onRejectClick,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF2F2F2),
                contentColor = Color.Black
            ),
            elevation = null
        ) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Từ chối", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun GetSentContact(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(color = Color(0xFFF2F2F2), shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Đã gửi lời mời",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF828282)
        )
    }
}

@Composable
fun ParametersRow(user: User?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(0.5.dp, Color(0xFFE5E5E5)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Ghim", user?.quantityPin ?: 0)
        StatItem("Lượt thích", user?.quantityReact ?: 0)
        StatItem("Bình luận", user?.quantityComment ?: 0)
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun SelfActionRow(
    pendingInvites: Int,
    onInvitesClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onInvitesClick) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = "Invites",
                    tint = Color(0xFF1665D8),
                    modifier = Modifier.size(24.dp)
                )

                if (pendingInvites > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .background(Color(0xFFFF4D4F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pendingInvites.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit profile",
                tint = Color(0xFF1665D8)
            )
        }
    }
}


// @Preview(showBackground = true)
// @Composable
// fun PreviewProfileScreen() {
//     LocationSocialTheme {
//         ProfileScreen(
//             1,
//             onEditClick = {},
//             onProfileClick = {},
//             onPressPost = {}
//         )
//     }
// }

// @Preview(showBackground = true)
// @Composable
// fun PreviewSelf() {
//     LocationSocialTheme {
//         ProfileSelfView(
//             user = User(
//                 userId = 1,
//                 userName = "linhnguyen",
//                 location = "Hồ Chí Minh, Việt Nam",
//                 avatarUrl = "https://example.com/avatar/linh.png",
//                 quote = "Sống là trải nghiệm.",
//                 name = "Nguyễn Thị Linh",
//                 quantityPin = 34,
//                 quantityReact = 1280,
//                 quantityComment = 256,
//                 userEmail = "linh.nguyen@example.com",
//                 phoneNumber = "+84 912 345 678",
//                 website = "https://linhnguyen.dev",
//                 quantityContact = 5
//             ),
//             onPressPost = {},
//             onInvitesClick = {},
//             onEditClick = {},
//             badges = emptyList(),
//             currentPosts = emptyList()
//         )
//     }
// }

// @Preview(showBackground = true)
// @Composable
// fun PreviewStranger() {
//     LocationSocialTheme {
//         ProfileStrangerView(
//             user = User(
//                 userId = 1,
//                 userName = "linhnguyen",
//                 location = "Hồ Chí Minh, Việt Nam",
//                 avatarUrl = "https://example.com/avatar/linh.png",
//                 quote = "Sống là trải nghiệm.",
//                 name = "Nguyễn Thị Linh",
//                 quantityPin = 34,
//                 quantityReact = 1280,
//                 quantityComment = 256,
//                 userEmail = "linh.nguyen@example.com",
//                 phoneNumber = "+84 912 345 678",
//                 website = "https://linhnguyen.dev",
//                 quantityContact = 5
//             ),
//             onRejectClick = {},
//             onAcceptClick = {},
//             onGetContactClick = {},
//             badges = TODO(),
//             modifier = TODO()
//         )
//     }
// }
