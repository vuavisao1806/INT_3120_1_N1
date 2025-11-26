package com.example.locationpins.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun StartCaptureButton(
    modifier: Modifier = Modifier,
    onStartCapture: () -> Unit
) {
    Button(
        onClick = onStartCapture,
        modifier = modifier
    ) {
        Text("Chụp ảnh")
    }
}
