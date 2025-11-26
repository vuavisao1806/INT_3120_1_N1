package com.example.locationpins.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock

@Composable
fun GridPostPreview(
    posts: List<Post>,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

//         Các item Post
        items(posts) { post ->
            PostPreviewForGrid(
                post = post,
                modifier = Modifier.wrapContentHeight()
            )
        }
    }
}



@Preview
@Composable

fun GridPostPreviewPreview() {
    GridPostPreview(
        PostMock.samplePosts
    )
}
