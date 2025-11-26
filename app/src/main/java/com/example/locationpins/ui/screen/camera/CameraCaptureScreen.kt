package com.example.locationpins.ui.screen.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CameraCaptureScreen(
    onImageCaptured: (Uri) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var overlaySquareSize by remember { mutableFloatStateOf(0f) }
    var overlaySquareLeft by remember { mutableFloatStateOf(0f) }
    var overlaySquareTop by remember { mutableFloatStateOf(0f) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    LaunchedEffect(cameraProviderFuture, lensFacing) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraCapture", "Use case binding failed", e)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    previewSize = size
                }
        )

        // Overlay với khung vuông
        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height

            // Khung vuông MAX = cạnh nhỏ nhất của màn hình (100%)
            val squareSize = min(screenWidth, screenHeight)

            overlaySquareSize = squareSize
            overlaySquareLeft = (screenWidth - squareSize) / 2
            overlaySquareTop = (screenHeight - squareSize) / 2

            // Vẽ viền trắng cho khung vuông
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(overlaySquareLeft, overlaySquareTop),
                size = Size(squareSize, squareSize),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 4f)
            )
        }

        // Nút đóng (X)
        IconButton(
            onClick = onCancel,
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

        // Row chứa nút chụp và nút đổi camera ở dưới
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer bên trái để căn giữa nút chụp
            Spacer(modifier = Modifier.width(80.dp))

            // Nút chụp ảnh (ở giữa)
            IconButton(
                onClick = {
                    val photoFile = File(
                        context.cacheDir,
                        "captured_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Log.e("CameraCapture", "Lỗi chụp ảnh: ${exc.message}", exc)
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                try {
                                    // Đọc và xoay ảnh
                                    var bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                                    val exif = ExifInterface(photoFile.absolutePath)
                                    val orientation = exif.getAttributeInt(
                                        ExifInterface.TAG_ORIENTATION,
                                        ExifInterface.ORIENTATION_NORMAL
                                    )
                                    val rotationDegrees = when (orientation) {
                                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                                        else -> 0
                                    }

                                    if (rotationDegrees != 0) {
                                        val matrix = Matrix().apply {
                                            postRotate(rotationDegrees.toFloat())
                                        }
                                        bitmap = Bitmap.createBitmap(
                                            bitmap, 0, 0,
                                            bitmap.width, bitmap.height,
                                            matrix, true
                                        )
                                    }

                                    // === FIX CHÍNH: Tính toán crop cho FIT_CENTER ===
                                    val bitmapWidth = bitmap.width.toFloat()
                                    val bitmapHeight = bitmap.height.toFloat()
                                    val bitmapAspect = bitmapWidth / bitmapHeight

                                    val previewWidth = previewSize.width.toFloat()
                                    val previewHeight = previewSize.height.toFloat()
                                    val previewAspect = previewWidth / previewHeight

                                    // Với FIT_CENTER: bitmap được scale để fit vào preview
                                    // Tính kích thước bitmap sau khi scale trong preview
                                    val (scaledBitmapWidth, scaledBitmapHeight) = if (bitmapAspect > previewAspect) {
                                        // Bitmap rộng hơn -> fit theo width, có letterbox trên/dưới
                                        previewWidth to (previewWidth / bitmapAspect)
                                    } else {
                                        // Bitmap cao hơn -> fit theo height, có letterbox trái/phải
                                        (previewHeight * bitmapAspect) to previewHeight
                                    }

                                    // Offset của bitmap trong preview (vùng letterbox)
                                    val previewOffsetX = (previewWidth - scaledBitmapWidth) / 2
                                    val previewOffsetY = (previewHeight - scaledBitmapHeight) / 2

                                    // Tỷ lệ từ preview coordinate sang bitmap coordinate
                                    val scale = bitmapWidth / scaledBitmapWidth

                                    // Chuyển đổi overlay coordinate sang bitmap coordinate
                                    val cropX = ((overlaySquareLeft - previewOffsetX) * scale).roundToInt()
                                    val cropY = ((overlaySquareTop - previewOffsetY) * scale).roundToInt()
                                    val cropSize = (overlaySquareSize * scale).roundToInt()

                                    // Đảm bảo không vượt biên
                                    val x = cropX.coerceIn(0, bitmap.width - 1)
                                    val y = cropY.coerceIn(0, bitmap.height - 1)
                                    val size = cropSize.coerceAtMost(
                                        min(bitmap.width - x, bitmap.height - y)
                                    )

                                    Log.d("CameraCapture", """
                                        Bitmap: ${bitmap.width}x${bitmap.height}
                                        Preview: ${previewSize.width}x${previewSize.height}
                                        Scaled in preview: ${scaledBitmapWidth}x${scaledBitmapHeight}
                                        Preview offset: ($previewOffsetX, $previewOffsetY)
                                        Overlay: ${overlaySquareSize}px at (${overlaySquareLeft}, ${overlaySquareTop})
                                        Crop: ${size}px at ($x, $y)
                                    """.trimIndent())

                                    val squareBitmap = Bitmap.createBitmap(
                                        bitmap, x, y, size, size
                                    )

                                    // Lưu ảnh vuông
                                    val squareFile = File(
                                        context.cacheDir,
                                        "captured_square_${System.currentTimeMillis()}.jpg"
                                    )
                                    FileOutputStream(squareFile).use { out ->
                                        squareBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                    }

                                    bitmap.recycle()
                                    squareBitmap.recycle()

                                    onImageCaptured(squareFile.toUri())

                                } catch (e: Exception) {
                                    Log.e("CameraCapture", "Lỗi xử lý ảnh: ${e.message}", e)
                                    val savedUri = output.savedUri ?: photoFile.toUri()
                                    onImageCaptured(savedUri)
                                }
                            }
                        }
                    )
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Chụp ảnh",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Nút đổi camera (bên phải nút chụp)
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Đổi camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}