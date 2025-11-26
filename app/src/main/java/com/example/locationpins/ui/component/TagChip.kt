package com.example.locationpins.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TagChip(
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "#$tag",
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0xFF1976D2).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = Color(0xFF1976D2),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}