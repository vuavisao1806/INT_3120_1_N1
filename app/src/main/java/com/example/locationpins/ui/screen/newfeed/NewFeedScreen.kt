package com.example.locationpins.ui.screen.newfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.ui.component.PostPreviewForNewsFeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    posts: List<Post> = PostMock.samplePosts,
    onPostClick: (Post) -> Unit = {},
    onReactClick: (Post) -> Unit = {},
    onCommentClick: (Post) -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = posts,
            key = { post -> post.postId }
        ) { post ->
            PostPreviewForNewsFeed(
                post = post,
                modifier = Modifier.padding(horizontal = 12.dp),
                onPostClick = { onPostClick(post) },
                onReactClick = { onReactClick(post) },
                onCommentClick = { onCommentClick(post) },
                onTagClick = onTagClick
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun NewsFeedScreenPreview() {
    NewsFeedScreen(
        posts = PostMock.samplePosts,
        onPostClick = { post -> println("Clicked post: ${post.title}") },
        onReactClick = { post -> println("Reacted to: ${post.title}") },
        onCommentClick = { post -> println("Comment on: ${post.title}") },
        onTagClick = { tag -> println("Clicked tag: $tag") }
    )
}