package com.example.locationpins.ui.screen.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.locationpins.R
import com.example.locationpins.data.model.User
import com.example.locationpins.ui.theme.LocationSocialTheme

@Composable
fun ProfileScreen(
    user: User,
    profileMode: ProfileMode,
    viewModel: ProfileViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    // lưu user vào viewModel
    LaunchedEffect(user.userId, profileMode) {
        viewModel.setUser(user, profileMode)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when (profileMode) {
            is ProfileMode.Self -> ProfileSelfView(
                user,
                onInvitesClick = {},
                onEditClick = {})
            is ProfileMode.Friend -> ProfileFriendView(user)
            ProfileMode.Stranger -> ProfileStrangerView(
                user,
                onClick = {viewModel.onGetContactClick()})
        }
        // Hiện form nhập yêu cầu liên hệ đối với người lạ
        if (uiState.showRequestContact) {
            RequestContactScreen(
                onDismiss = {
                   viewModel.onDismissRequestDialog()
                },
                onSend = { viewModel.onSendClick() },
                onMessageChange = { viewModel.onMessageChange(it) },
                message = viewModel.getMessage()
            )
        }
    }
}

// Màn hình cho bản thân
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileSelfView(
    user: User,
    onInvitesClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.background
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        stickyHeader {
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
                        pendingInvites = user.quantityContact,
                        onInvitesClick = onInvitesClick,
                        onEditClick = onEditClick
                    )
                }
            }
        }

        item { AvatarAndNameColumn(user) }
        item { InfoUserRow(user) }
        item { ParametersRow(user) }
    }
}

// Màn hình cho bạn bè
@Composable
fun ProfileFriendView(user: User, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarAndNameColumn(user)
        InfoUserRow(user)
        ParametersRow(user)
    }
}
// Màn hình cho người lạ
@Composable
fun ProfileStrangerView(user: User, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarAndNameColumn(user)
        GetContactButton(onClick = onClick)
        ParametersRow(user)
    }
}

@Composable
fun AvatarAndNameColumn(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.avatarUrl)
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
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${user.userName}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = user.quote,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4C4C4C)
        )
    }
}

@Composable
fun InfoUserRow(user: User, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfoLine("📍", user.location)
        InfoLine("📧", user.userEmail)
        InfoLine("📱", user.phoneNum)
        InfoLine("🌐", user.website)
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
fun GetContactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text("Get contact")
    }
}

@Composable
fun ParametersRow(user: User, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .border(0.5.dp, Color(0xFFE5E5E5)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Ghim", user.quantityPin)
        StatItem("Lượt thích", user.quantityReact)
        StatItem("Bình luận", user.quantityComment)
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
// Hàng chứa button danh sách liên hệ và edit (đối với bản thân)
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

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    LocationSocialTheme {
        ProfileScreen( user = User(
            userId = "u001",
            userName = "linhnguyen",
            location = "Hồ Chí Minh, Việt Nam",
            avatarUrl = "https://example.com/avatar/linh.png",
            quote = "Sống là trải nghiệm.",
            name = "Nguyễn Thị Linh",
            quantityPin = 34,
            quantityReact = 1280,
            quantityComment = 256,
            userEmail = "linh.nguyen@example.com",
            phoneNum = "+84 912 345 678",
            website = "https://linhnguyen.dev",
            quantityContact = 5
        ),
            profileMode = ProfileMode.Stranger)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelf() {
    LocationSocialTheme {
        ProfileSelfView(
            user = User(
                userId = "u001",
                userName = "linhnguyen",
                location = "Hồ Chí Minh, Việt Nam",
                avatarUrl = "https://example.com/avatar/linh.png",
                quote = "Sống là trải nghiệm.",
                name = "Nguyễn Thị Linh",
                quantityPin = 34,
                quantityReact = 1280,
                quantityComment = 256,
                userEmail = "linh.nguyen@example.com",
                phoneNum = "+84 912 345 678",
                website = "https://linhnguyen.dev",
                quantityContact = 5
            ),
            onInvitesClick = {},
            onEditClick = {}

        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStranger() {
    LocationSocialTheme {
        ProfileStrangerView(
            user = User(
                userId = "u001",
                userName = "linhnguyen",
                location = "Hồ Chí Minh, Việt Nam",
                avatarUrl = "https://example.com/avatar/linh.png",
                quote = "Sống là trải nghiệm.",
                name = "Nguyễn Thị Linh",
                quantityPin = 34,
                quantityReact = 1280,
                quantityComment = 256,
                userEmail = "linh.nguyen@example.com",
                phoneNum = "+84 912 345 678",
                website = "https://linhnguyen.dev",
                quantityContact = 5
            ),
            onClick = {}
        )
    }
}
