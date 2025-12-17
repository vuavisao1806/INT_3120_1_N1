package com.example.locationpins.ui.screen.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.locationpins.R
import com.example.locationpins.data.model.User
import com.example.locationpins.data.remote.dto.user.LoginRequest
import com.example.locationpins.data.remote.dto.user.ShowContactRespond


@Composable
fun ContactRequestItem(
    request: ShowContactRespond,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onShowProfileClick:()-> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp).clickable(){onShowProfileClick()},
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Avatar
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(request.avatarUrl)
                .crossfade(true)
                .build(),
            error = painterResource(R.drawable.ic_launcher_background), // Thay ảnh lỗi của bạn
            placeholder = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Thông tin và Nút bấm
        Column(modifier = Modifier.weight(1f)) {
            // Tên và Thời gian
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = request.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = request.timeCreate, // Ví dụ: "2 giờ trước"
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Logic hiển thị Nút hoặc Text trạng thái
            when (request.status) {
                "PENDING" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1665D8)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Chấp nhận", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Xóa", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                "ACCEPTED" -> {
                    Text(
                        text = "Đã chấp nhận",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1665D8), // Màu xanh
                        fontWeight = FontWeight.Medium
                    )
                }

                "CANCELED" -> {
                    Text(
                        text = "Đã từ chối",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRequestsSheet(
    requests: List<ShowContactRespond>,
    onDismiss: () -> Unit,
    onAccept: (ShowContactRespond) -> Unit,
    onReject: (ShowContactRespond) -> Unit,
    onShowProfileClick: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {

            Text(
                text = "Yêu cầu kết bạn (${requests.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )


            // Danh sách yêu cầu
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 50.dp)
            ) {
                items(requests.size) { index ->
                    val user = requests[index]
                    ContactRequestItem(
                        request = user,
                        onAccept = { onAccept(user) },
                        onReject = { onReject(user) },
                        onShowProfileClick = {onShowProfileClick(user.followingUserId)}
                    )
                    Divider(color = Color(0xFFF0F0F0), thickness = 0.5.dp)
                }
            }
        }

    }
}