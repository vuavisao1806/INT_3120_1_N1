package com.example.locationpins.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.utils.formatCount
import kotlin.math.max


@Composable
fun PostPreviewForGrid(
    post: Post,
    modifier: Modifier = Modifier,
    onPress: () -> Unit = {}
) {
    val density = LocalDensity.current
    var widthDp by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { size ->
                widthDp = with(density) { size.width.toDp() }
            }
            .clickable { onPress() }
    ) {
        // Tính toán size dựa trên độ rộng với giá trị min
        val iconSize = max(widthDp.value * 0.14f, 24f).dp
        val fontSize = max(widthDp.value * 0.055f, 12f).sp
        val spacingSmall = max(widthDp.value * 0.006f, 2f).dp
        val spacingLarge = max(widthDp.value * 0.025f, 6f).dp
        val edgePadding = max(widthDp.value * 0.04f, 8f).dp

        // Ảnh nền
        Image(
            painter = rememberAsyncImagePainter(post.imageUrl),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        // Icon + số react/comment
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = edgePadding, bottom = edgePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Reacts",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(spacingSmall))
            Text(
                text = formatCount(post.reactCount as Int),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(spacingLarge))

            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.height(spacingSmall))
            Text(
                text = formatCount(post.commentCount as Int),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview
@Composable
fun PostPreviewForGridPreview(){
    PostPreviewForGrid(
        post = PostMock.samplePosts.first(),
        modifier = Modifier,
    )
}