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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock

@Composable
fun PostPreview(
    post: Post,
    modifier: Modifier = Modifier,
    onPress: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clickable { onPress() }
    ) {
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
                .padding(end = 8.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Reacts",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = post.reactCount.toString(),
                color = Color.White,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(9.dp))

            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = post.commentCount.toString(),
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

@Preview
@Composable
fun PostPreviewPreView(){
    PostPreview(
        post = PostMock.samplePosts.first(),
        modifier = Modifier,
    )
}