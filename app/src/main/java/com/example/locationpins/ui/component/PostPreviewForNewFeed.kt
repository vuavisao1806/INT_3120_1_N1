package com.example.locationpins.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.utils.formatCount
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostPreviewForNewsFeed(
    post: Post,
    modifier: Modifier = Modifier,
    onPostPress: () -> Unit = {},
    onReactPress: () -> Unit = {},
    onCommentPress: () -> Unit = {},
    onTagPress: (String) -> Unit = {}
) {
    //Bài viết dài có đang ở trạng thái mở rộng (hiển thị tất cả hay không)
    var isExpanded by remember { mutableStateOf(false) }
    val maxCollapsedLines = 3
    val cardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostPress() },
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ảnh bài viết
            DynamicAsyncImage(
                imageUrl = post.imageUrl,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            )
//            Image(
//                painter = rememberAsyncImagePainter(post.imageUrl),
//                contentDescription = post.title,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(4f / 3f),
//                contentScale = ContentScale.Crop
//            )

            // Nội dung bài viết
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content với "Xem thêm"
                Column {
                    Text(
                        text = post.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        maxLines = if (isExpanded) Int.MAX_VALUE else maxCollapsedLines,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )

                    // Nút "Xem thêm" / "Thu gọn"
                    if (post.body.length > 100) {
                        Text(
                            text = if (isExpanded) "Thu gọn" else "Xem thêm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isExpanded = !isExpanded }
                        )
                    }
                }

                // Tags
                if (post.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        post.tags.forEach { tag ->
                            TagChip(
                                tag = tag,
                                onClick = { onTagPress(tag) }
                            )
                        }
                    }
                }
            }

            // Separator
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            // Action bar (React & Comment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // React button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onReactPress() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "React",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatCount(post.reactCount as Int),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comment button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCommentPress() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatCount(post.commentCount as Int),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostPreviewForNewsFeedPreview() {
    PostPreviewForNewsFeed(
        post = PostMock.samplePosts.first(),
        modifier = Modifier.padding(8.dp),
        onTagPress = { tag -> println("Clicked tag: $tag") }
    )
}