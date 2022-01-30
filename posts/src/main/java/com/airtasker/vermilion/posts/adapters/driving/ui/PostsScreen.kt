package com.airtasker.vermilion.posts.adapters.driving.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.airtasker.vermilion.posts.R
import com.airtasker.vermilion.posts.domain.entities.Post
import com.airtasker.vermilion.ui.theme.VermilionTheme

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
            is PostsScreenState.NoPosts -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(id = R.string.posts_screen_no_posts),
                        style = MaterialTheme.typography.h3,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
            }
            is PostsScreenState.Error -> TODO()
            is PostsScreenState.Loading -> TODO()
            is PostsScreenState.Posts -> TODO()
        }

    }
}

sealed class PostsScreenState {
    object NoPosts : PostsScreenState()
    data class Error(val message: String) : PostsScreenState()
    object Loading : PostsScreenState()
    data class Posts(val posts: List<Post>) : PostsScreenState()
}

@Preview(name = "Posts light theme")
@Composable
fun PostsScreenPreview() {
    VermilionTheme {
        PostsScreen(
            "Subreddit",
            PostsScreenState.NoPosts
        )
    }
}

@Preview(name = "Posts dark theme")
@Composable
fun PostsScreenPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostsScreen(
            "Subreddit",
            PostsScreenState.NoPosts
        )
    }
}