package com.example.locationpins.ui.screen.camera

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.example.locationpins.ui.screen.map.LocationManager
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    RequestLocationPermission(
        onGranted = { LocationManager.onPermissionGranted() },
        onDenied = {}
    )

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope() // Coroutine scope để chạy việc chờ GPS

    // State quản lý việc đang chờ GPS khi bấm chụp
    var isLoadingLocationOnCapture by remember { mutableStateOf(false) }

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

    // Setup Camera
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

    val captureImage = {
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
                    isLoadingLocationOnCapture = false
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        // Xử lý xoay và crop ảnh
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

                        // Tính toán crop
                        val bitmapWidth = bitmap.width.toFloat()
                        val bitmapHeight = bitmap.height.toFloat()
                        val bitmapAspect = bitmapWidth / bitmapHeight
                        val previewWidth = previewSize.width.toFloat()
                        val previewHeight = previewSize.height.toFloat()
                        val previewAspect = previewWidth / previewHeight

                        val (scaledBitmapWidth, scaledBitmapHeight) = if (bitmapAspect > previewAspect) {
                            previewWidth to (previewWidth / bitmapAspect)
                        } else {
                            (previewHeight * bitmapAspect) to previewHeight
                        }

                        val previewOffsetX = (previewWidth - scaledBitmapWidth) / 2
                        val previewOffsetY = (previewHeight - scaledBitmapHeight) / 2
                        val scale = bitmapWidth / scaledBitmapWidth

                        val cropX = ((overlaySquareLeft - previewOffsetX) * scale).roundToInt()
                        val cropY = ((overlaySquareTop - previewOffsetY) * scale).roundToInt()
                        val cropSize = (overlaySquareSize * scale).roundToInt()

                        val x = cropX.coerceIn(0, bitmap.width - 1)
                        val y = cropY.coerceIn(0, bitmap.height - 1)
                        val size = cropSize.coerceAtMost(
                            min(bitmap.width - x, bitmap.height - y)
                        )

                        val squareBitmap = Bitmap.createBitmap(
                            bitmap, x, y, size, size
                        )

                        val squareFile = File(
                            context.cacheDir,
                            "captured_square_${System.currentTimeMillis()}.jpg"
                        )
                        FileOutputStream(squareFile).use { out ->
                            squareBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                        }

                        bitmap.recycle()
                        squareBitmap.recycle()

                        isLoadingLocationOnCapture = false
                        onImageCaptured(squareFile.toUri())

                    } catch (e: Exception) {
                        Log.e("CameraCapture", "Lỗi xử lý ảnh: ${e.message}", e)
                        isLoadingLocationOnCapture = false
                        val savedUri = output.savedUri ?: photoFile.toUri()
                        onImageCaptured(savedUri)
                    }
                }
            }
        )
    }

    // UI CHÍNH
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size -> previewSize = size }
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val screenWidth = size.width
            val screenHeight = size.height
            val squareSize = min(screenWidth, screenHeight)

            overlaySquareSize = squareSize
            overlaySquareLeft = (screenWidth - squareSize) / 2
            overlaySquareTop = (screenHeight - squareSize) / 2

            // Vẽ vùng tối
            drawRect(Color.Black.copy(alpha = 0.7f), Offset(0f, 0f), Size(screenWidth, overlaySquareTop))
            drawRect(Color.Black.copy(alpha = 0.7f), Offset(0f, overlaySquareTop + squareSize), Size(screenWidth, screenHeight - overlaySquareTop - squareSize))
            drawRect(Color.Black.copy(alpha = 0.7f), Offset(0f, overlaySquareTop), Size(overlaySquareLeft, squareSize))
            drawRect(Color.Black.copy(alpha = 0.7f), Offset(overlaySquareLeft + squareSize, overlaySquareTop), Size(screenWidth - overlaySquareLeft - squareSize, squareSize))

            // Vẽ viền trắng
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(overlaySquareLeft, overlaySquareTop),
                size = Size(squareSize, squareSize),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 4f)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            val currentLocation by LocationManager.location.collectAsState()

            if (currentLocation == null) {
                // Đang tìm GPS
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang định vị...",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isLoadingLocationOnCapture) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)), // Làm tối màn hình
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang chờ tín hiệu GPS...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Đóng", tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(80.dp))

            // Nút Chụp Ảnh
            IconButton(
                onClick = {
                    if (isLoadingLocationOnCapture) return@IconButton // Chống spam nút

                    val loc = LocationManager.location.value
                    if (loc != null) {
                        // CASE A: Đã có vị trí -> Chụp ngay lập tức
                        captureImage()
                    } else {
                        // CASE B: Chưa có vị trí -> Hiện loading và chờ
                        isLoadingLocationOnCapture = true
                        scope.launch {
                            // Treo ở đây cho đến khi location khác null
                            LocationManager.location.filterNotNull().first()

                            // Có location rồi -> Chụp
                            captureImage()
                        }
                    }
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    "Chụp ảnh",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Nút Đổi Camera
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
                    Icons.Default.Cameraswitch,
                    "Đổi camera",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
) {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) onGranted() else onDenied()
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}