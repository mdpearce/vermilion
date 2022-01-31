package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@Composable
fun PostsScreen(
    title: String,
    state: PostsScreenState
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
        topBar = {
            TopAppBar(title = {
                Text(title)
            })
        },
        bottomBar = {
            BottomAppBar {

            }
        }
    ) {

        when (state) {
            is PostsScreenState.Empty -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {

                }
            }
            is PostsScreenState.Error -> TODO()
            is PostsScreenState.Posts -> {
                LazyColumn {
                    items(state.posts) {
                        PostCard(post = it)
                    }
                }
            }
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
        PostsScreen(
            "Subreddit",
            PostsScreenState.Posts(listOf(DUMMY_TEXT_POST))
        )
    }
}

@Preview(name = "Posts dark theme")
@Composable
fun PostsScreenPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostsScreen(
            "Subreddit",
            PostsScreenState.Posts(listOf(DUMMY_TEXT_POST))
        )
    }
}