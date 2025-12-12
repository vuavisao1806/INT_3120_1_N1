package com.example.locationpins.ui.screen.createPost

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.locationpins.data.model.User
import com.example.locationpins.data.model.UserMock
import com.example.locationpins.ui.screen.camera.CameraWithPermission

enum class CreatePostStep {
    Editing,
    Capturing
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialImageUri: Uri?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    user: User
) {
    val context = LocalContext.current

    val viewModel: CreatePostViewModel = viewModel()

    var step by remember { mutableStateOf(CreatePostStep.Editing) }
    var currentImageUri by remember { mutableStateOf(initialImageUri) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when (step) {
            CreatePostStep.Editing -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Tạo bài đăng",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    enabled = !isPosting,
                                    onClick = onNavigateBack
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Đóng"
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        val img = currentImageUri ?: return@IconButton
                                        isPosting = true
                                        viewModel.submitPost(
                                            context = context,
                                            userId = user.userId, // đổi nếu field khác
                                            title = title,
                                            content = content,
                                            imageUri = img,
                                            status = "active",
                                            onSuccess = {
                                                isPosting = false
                                                Toast.makeText(
                                                    context,
                                                    "Đăng bài thành công",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onNavigateBack()
                                            },
                                            onError = { msg ->
                                                isPosting = false
                                                Toast.makeText(
                                                    context,
                                                    msg,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    },
                                    enabled = !isPosting && currentImageUri != null && title.isNotBlank()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Đăng",
                                        tint = if (!isPosting && currentImageUri != null && title.isNotBlank())
                                            Color(0xFF1DA1F2)
                                        else
                                            Color.Gray
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .background(Color.White),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // User profile section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF667EEA),
                                                Color(0xFF764BA2)
                                            )
                                        )
                                    )
                            )

                            Column {
                                Text(
                                    user.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Text(
                                    "@${user.userName}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Image section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            if (currentImageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(currentImageUri),
                                        contentDescription = "Ảnh đã chụp",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = {
                                            if (!isPosting) step = CreatePostStep.Capturing
                                        },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                            .size(56.dp)
                                            .background(
                                                Color.White,
                                                RoundedCornerShape(28.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Chụp lại",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(enabled = !isPosting) {
                                            step = CreatePostStep.Capturing
                                        }
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Chụp ảnh",
                                            tint = Color(0xFF9E9E9E),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            "Nhấn để chụp ảnh",
                                            color = Color(0xFF757575),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Title input
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            placeholder = {
                                Text(
                                    "Tiêu đề bài đăng...",
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            singleLine = true,
                            enabled = !isPosting,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            )
                        )

                        // Content input
                        TextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .weight(1f, fill = false),
                            placeholder = {
                                Text(
                                    "Viết nội dung cho bài đăng...",
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            maxLines = 10,
                            enabled = !isPosting,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            )
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            CreatePostStep.Capturing -> {
                CameraWithPermission(
                    onImageCaptured = { uri ->
                        currentImageUri = uri
                        step = CreatePostStep.Editing
                    },
                    onCancel = {
                        step = CreatePostStep.Editing
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview
@Composable
fun CreatePostScreenPreview() {
    CreatePostScreen(
        initialImageUri = null,
        onNavigateBack = {},
        user = UserMock.sampleUser.first()
    )
}
