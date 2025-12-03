package com.example.locationpins.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Form nhập yêu cầu liên hệ
@Composable
fun RequestContactScreen(
    message: String,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,

    ) {

    val maxChars = 500
    val canSend = message.isNotBlank() && message.length <= maxChars

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gửi yêu cầu kết nối",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Đóng"
                        )
                    }
                }

                Divider(color = Color(0xFFE5E5E5))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Giới thiệu bản thân",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            if (it.length <= maxChars) {
                                onMessageChange(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = {
                            Text(
                                "Hãy viết vài dòng giới thiệu về bản thân để người nhận hiểu rõ hơn về bạn...",
                                color = Color(0xFFB0B8C5),
                                fontSize = 13.sp
                            )
                        },
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "${message.length}/$maxChars ký tự",
                        fontSize = 12.sp,
                        color = Color(0xFF9AA4B2)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = { onSend() },
                        enabled = canSend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1665D8),
                            disabledContainerColor = Color(0xFFE1E5EB),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF9AA4B2)
                        )
                    ) {
                        Text("Gửi")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1024, heightDp = 512)
@Composable
fun PreviewConnectionRequestDialog() {
    MaterialTheme {
        var msg = remember { mutableStateOf("") }

        RequestContactScreen(
            message = msg.value,
            onMessageChange = { msg.value = it },
            onDismiss = {},
            onSend = {}
        )
    }
}