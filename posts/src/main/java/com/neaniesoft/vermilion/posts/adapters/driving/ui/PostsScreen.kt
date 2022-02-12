package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.pageFlow.collectAsLazyPagingItems()
    Box {
        PostsList(posts = pagingItems)
    }
}

@Composable
fun PostsList(
    posts: LazyPagingItems<Post>
) {
    LazyColumn {
        items(posts) { post ->
            Spacer(Modifier.height(12.dp))
            if (post != null) {
                PostCard(post = post)
            } else {
                PostCardPlaceholder()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(name = "Posts light theme")
@Composable
fun PostsScreenPreview() {
    VermilionTheme {
        PostsList(
            flowOf(
                PagingData.from(
                    listOf(
                        DUMMY_TEXT_POST,
                        DUMMY_TEXT_POST
                    )
                )
            ).collectAsLazyPagingItems()
        )
    }
}

@Preview(name = "Posts dark theme")
@Composable
fun PostsScreenPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostsList(
            flowOf(
                PagingData.from(
                    listOf(
                        DUMMY_TEXT_POST,
                        DUMMY_TEXT_POST
                    )
                )
            ).collectAsLazyPagingItems()
        )
    }
}
