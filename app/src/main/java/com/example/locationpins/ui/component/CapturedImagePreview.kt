package com.example.locationpins.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun CapturedImagePreview(
    imageUri: Uri?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .background(
                color = Color(0xFF101010),
                shape = MaterialTheme.shapes.medium
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Text(
                text = "Chưa có ảnh nào",
                color = Color.LightGray
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
