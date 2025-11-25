package com.example.locationpins.ui.component

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.locationpins.ui.screen.camera.CameraWithPermission

@Composable
fun CameraCaptureLauncher(
    modifier: Modifier = Modifier,
    onImageCaptured: (Uri) -> Unit
) {
    var showCamera by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        // Nút khởi động chụp ảnh
        Button(
            onClick = { showCamera = true },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("Chụp ảnh")
        }

        // Overlay camera (full screen) khi bấm nút
        if (showCamera) {
            CameraWithPermission(
                onImageCaptured = { uri ->
                    onImageCaptured(uri)
                    showCamera = false
                },
                onCancel = {
                    showCamera = false
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
