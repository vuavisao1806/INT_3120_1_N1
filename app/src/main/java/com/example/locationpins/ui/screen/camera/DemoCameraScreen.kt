package com.example.locationpins.ui.screen.camera

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.locationpins.ui.component.CapturedImagePreview
import com.example.locationpins.ui.component.StartCaptureButton
import com.example.locationpins.ui.screen.camera.CameraWithPermission

enum class DemoCameraStep {
    Idle,
    Capturing
}

@Composable
fun DemoCameraScreen(
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(DemoCameraStep.Idle) }
    var capturedUri by remember { mutableStateOf<Uri?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (step) {
            DemoCameraStep.Idle -> {
                // Màn chính: Nút chụp + preview ảnh bên dưới
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Demo Camera Screen")

                    StartCaptureButton(
                        onStartCapture = {
                            step = DemoCameraStep.Capturing
                        }
                    )

                    CapturedImagePreview(
                        imageUri = capturedUri,
                        modifier = Modifier
                    )
                }
            }

            DemoCameraStep.Capturing -> {

                CameraWithPermission(
                    onImageCaptured = { uri ->
                        capturedUri = uri
                        step = DemoCameraStep.Idle   // quay về màn chính và hiển thị ảnh
                    },
                    onCancel = {
                        step = DemoCameraStep.Idle   // không chụp nữa, quay lại
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
