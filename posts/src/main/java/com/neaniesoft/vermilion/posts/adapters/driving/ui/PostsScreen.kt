package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@Composable
fun PostsScreen(
    viewModel: PostsViewModel = viewModel()
) {
    val state = viewModel.state.collectAsState()

    Box {
        when (val currentState = state.value) {
            is PostsScreenState.Empty -> {
                EmptyPostsScreen(currentState.isLoading)
            }
            is PostsScreenState.Error -> TODO()
            is PostsScreenState.Posts -> {
                PostsList(posts = currentState.posts)
            }
        }
    }
}

@Composable
fun EmptyPostsScreen(
    isLoading: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

@Composable
fun PostsList(
    posts: List<Post>
) {
    LazyColumn {
        itemsIndexed(posts) { index, post ->
            PostCard(post = post)
            if (index != posts.size - 1) Spacer(Modifier.height(24.dp))
        }
    }
}

sealed class PostsScreenState {
    data class Empty(val isLoading: Boolean = false) : PostsScreenState()
    data class Error(val message: String, val isLoading: Boolean = false) : PostsScreenState()
    data class Posts(val posts: List<Post>, val isLoading: Boolean = false) : PostsScreenState()
}

@Preview(name = "Posts light theme")
@Composable
fun PostsScreenPreview() {
    VermilionTheme {
        PostsList(listOf(DUMMY_TEXT_POST, DUMMY_TEXT_POST))
    }
}

@Preview(name = "Posts dark theme")
@Composable
fun PostsScreenPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostsList(listOf(DUMMY_TEXT_POST, DUMMY_TEXT_POST))
    }
}