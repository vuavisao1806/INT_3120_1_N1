package com.example.locationpins.ui.screen.pinDiscovery

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationpins.ui.component.SimpleCompass
import kotlinx.coroutines.delay

@Composable
fun PinDiscoveryScreen(
    onDismiss: () -> Unit,
    onPinFound: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PinDiscoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initCompass(context)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState.gameState) {
            GameState.Initial -> {
                // Full screen selection
                DistanceSelectionScreen(
                    selectedDistance = uiState.selectedDistance,
                    onDistanceSelected = { viewModel.selectDistance(it) },
                    onStartGame = { viewModel.startGame() },
                    onClose = {
                        viewModel.resetGame()
                        onDismiss()
                    },
                    isLoading = uiState.isLoading,
                    error = uiState.error
                )
            }

            GameState.Searching -> {
                // Compact floating compass
                FloatingCompassView(
                    currentDistance = uiState.currentDistance,
                    lastHint = uiState.lastHint,
                    compassRotation = uiState.compassRotation,
                    onEndSearch = {
                        viewModel.resetGame()
                        onDismiss()
                    }
                )
            }

            GameState.Found -> {
                // Full screen found dialog
                FoundScreen(
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
}

// ==========================================
// DISTANCE SELECTION SCREEN (Full Screen)
// ==========================================

@Composable
private fun DistanceSelectionScreen(
    selectedDistance: Int,
    onDistanceSelected: (Int) -> Unit,
    onStartGame: () -> Unit,
    onClose: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
    ) {
        IconButton(
            onClick = onClose,
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
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

                if (error != null) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

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

// ==========================================
// FLOATING COMPASS VIEW (Compact)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingCompassView(
    currentDistance: Float?,
    lastHint: String?,
    compassRotation: Float,
    onEndSearch: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showHintBubble by remember { mutableStateOf(false) }
    var lastShownHint by remember { mutableStateOf<String?>(null) }

    // Show bubble when new hint arrives
    LaunchedEffect(lastHint) {
        if (lastHint != null && lastHint != lastShownHint) {
            lastShownHint = lastHint
            showHintBubble = true
            delay(4000) // Hide after 4 seconds
            showHintBubble = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Floating compass button (top-right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hint bubble (above compass)
            AnimatedVisibility(
                visible = showHintBubble && lastHint != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 20.sp
                        )
                        Text(
                            text = lastHint ?: "",
                            fontSize = 13.sp,
                            color = Color.Black,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Compass button
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { isExpanded = true }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                SimpleCompass(
                    rotation = compassRotation,
                    size = 84.dp
                )

//                // Distance indicator
//                if (currentDistance != null && currentDistance < 100) {
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .size(24.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF4CAF50)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "${currentDistance.toInt()}m",
//                            fontSize = 8.sp,
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
            }
        }

        // Expanded bottom sheet
        if (isExpanded) {
            ModalBottomSheet(
                onDismissRequest = { isExpanded = false },
                containerColor = Color.White,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ExpandedGameView(
                    currentDistance = currentDistance,
                    lastHint = lastHint,
                    compassRotation = compassRotation,
                    onEndSearch = {
                        isExpanded = false
                        onEndSearch()
                    },
                    onClose = { isExpanded = false }
                )
            }
        }
    }
}

// ==========================================
// EXPANDED GAME VIEW (Bottom Sheet Content)
// ==========================================

@Composable
private fun ExpandedGameView(
    currentDistance: Float?,
    lastHint: String?,
    compassRotation: Float,
    onEndSearch: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đang Tìm Kiếm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.Gray
                )
            }
        }

        Divider(color = Color.LightGray.copy(alpha = 0.3f))

        // Large compass
        SimpleCompass(
            rotation = compassRotation,
            size = 160.dp
        )

//        // Distance info
//        if (currentDistance != null) {
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color(0xFFE3F2FD)
//                ),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Khoảng cách:",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//                    Text(
//                        text = "${currentDistance.toInt()} mét",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF1976D2)
//                    )
//                }
//            }
//        }

        // Latest hint
        if (lastHint != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Gợi ý mới nhất:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lastHint,
                        fontSize = 15.sp,
                        color = Color(0xFFE65100),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Text(
            text = "💡 Gợi ý mới sẽ xuất hiện sau mỗi 5 giây",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // End search button
        Button(
            onClick = onEndSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935)
            )
        ) {
            Text(
                text = "Kết thúc tìm kiếm",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// FOUND SCREEN (Full Screen)
// ==========================================

@Composable
private fun FoundScreen(
    onViewPin: () -> Unit,
    onPlayAgain: () -> Unit
) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
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
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎉", fontSize = 60.sp)
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

                Button(
                    onClick = onViewPin, // TODO: nối onViewPin sang màn hình xem ghim từ pin
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
}