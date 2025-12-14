package com.example.locationpins.ui.screen.pinDiscovery

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.ui.component.SimpleCompass

@Composable
fun PinDiscoveryScreen(
    onDismiss: () -> Unit,
    onPinFound: (Int) -> Unit, // Navigate to gallery with pinId
    modifier: Modifier = Modifier,
    viewModel: PinDiscoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initCompass(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 50.dp)
    ) {
        // Close button
        IconButton(
            onClick = {
                viewModel.resetGame()
                onDismiss()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Đóng",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Main content
        when (uiState.gameState) {
            GameState.Initial -> DistanceSelectionScreen(
                selectedDistance = uiState.selectedDistance,
                onDistanceSelected = { viewModel.selectDistance(it) },
                onStartGame = { viewModel.startGame() },
                isLoading = uiState.isLoading,
                error = uiState.error
            )

            GameState.Searching -> SearchingScreen(
                currentDistance = uiState.currentDistance,
                lastHint = uiState.lastHint,
                compassRotation = uiState.compassRotation
            )

            GameState.Found -> FoundScreen(
                onViewPin = {
                    uiState.targetPinId?.let { pinId ->
                        viewModel.resetGame()
                        onPinFound(pinId)
                    }
                },
                onPlayAgain = {
                    viewModel.resetGame()
                }
            )
        }
    }
}

@Composable
private fun DistanceSelectionScreen(
    selectedDistance: Int,
    onDistanceSelected: (Int) -> Unit,
    onStartGame: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(40.dp)
                )
            }

            // Title
            Text(
                text = "Khám phá Ghim Gần Đây",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Chọn khoảng cách để bắt đầu cuộc phiêu lưu!",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // Distance options
            val distances = listOf(50, 100, 200, 500)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                distances.forEach { distance ->
                    DistanceOption(
                        distance = distance,
                        isSelected = distance == selectedDistance,
                        onClick = { onDistanceSelected(distance) }
                    )
                }
            }

            // Error message
            if (error != null) {
                Log.d("ACB",error)
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Start button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Bắt đầu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceOption(
    distance: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1976D2).copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isSelected) Color(0xFF1976D2) else Color.LightGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${distance}m",
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1976D2) else Color.Black
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchingScreen(
    currentDistance: Float?,
    lastHint: String?,
    compassRotation: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // La bàn hoạt động
            SimpleCompass(
                rotation = compassRotation,
                size = 170.dp
            )

            Text(
                text = "Đang Tìm Kiếm...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            if (lastHint != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Text(
                        text = lastHint,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        color = Color(0xFFE65100),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                text = "💡 Gợi ý mới sẽ xuất hiện sau mỗi 30 giây",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
private fun FoundScreen(
    onViewPin: () -> Unit,
    onPlayAgain: () -> Unit
) {
    // Success animation
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉",
                    fontSize = 60.sp
                )
            }

            Text(
                text = "Chúc Mừng!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Bạn đã tìm thấy ghim ẩn!",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            // Action buttons
            Button(
                onClick = onViewPin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Text(
                    text = "Xem Ghim",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1976D2)
                )
            ) {
                Text(
                    text = "Chơi Lại",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
