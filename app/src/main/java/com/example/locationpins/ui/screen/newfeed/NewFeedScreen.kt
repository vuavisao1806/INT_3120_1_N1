package com.example.locationpins.ui.screen.newfeed

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationpins.data.model.Post
import com.example.locationpins.data.model.PostMock
import com.example.locationpins.ui.component.PostPreview

@Composable
fun NewFeedScreen(
    posts: List<Post>,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        item {
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )
        }

//         Các item Post
        items(posts) { post ->
            PostPreview(
                post = post,
                modifier = Modifier.wrapContentHeight()
            )
        }
    }
}



@Preview
@Composable

fun NewFeedScreenPreview() {
    NewFeedScreen(
        PostMock.samplePosts
    )
}
