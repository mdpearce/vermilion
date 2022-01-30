package com.airtasker.vermilion.posts.adapters.driving.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.airtasker.vermilion.ui.theme.VermilionTheme

@Composable
fun PostsScreen(title: String) {
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
            BottomAppBar() {

            }
        }
    ) {

    }
}

@Preview(name = "Posts light theme")
@Composable
fun PostsScreenPreview() {
    VermilionTheme {
        PostsScreen("Subreddit")
    }
}

@Preview(name = "Posts dark theme")
@Composable
fun PostsScreenPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostsScreen("Subreddit")
    }
}