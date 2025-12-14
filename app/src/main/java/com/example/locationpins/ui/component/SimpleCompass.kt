package com.example.locationpins.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.locationpins.R

@Composable
internal fun SimpleCompass(
    rotation: Float,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(10.dp, CircleShape)              // bóng đổ mềm
            .clip(CircleShape)
            .background(Color(0xFFF7F2E8))          // nền nhẹ (tuỳ)
            .border(2.dp, Color.White.copy(0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.compass_gold),
            contentDescription = "Compass",
            modifier = Modifier
                .fillMaxSize()
                // Nếu rotation bạn nhận là "azimuth" (hướng điện thoại đang quay),
                // thường bạn cần xoay NGƯỢC để kim/ảnh chỉ đúng hướng bắc.
                .graphicsLayer { rotationZ = -rotation }
        )

        // Optional: điểm nhấn ở tâm (cho cảm giác 3D)
        Box(
            modifier = Modifier
                .size(size * 0.10f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
        )
    }
}
