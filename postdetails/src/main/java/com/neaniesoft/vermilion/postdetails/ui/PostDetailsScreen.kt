package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.posts.ui.PostSummary

@Composable
fun PostDetailsScreen(
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onOpenUri: (Uri) -> Unit
) {
    val postDetailsState by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()

    LazyColumn {
        when (val currentPostDetailsState = postDetailsState) {
            is PostDetails -> {
                item {
                    PostSummary(
                        post = currentPostDetailsState.post,
                        shouldTruncate = false,
                        shouldHideNsfw = false,
                        onMediaClicked = { onOpenUri(it.link.toString().toUri()) }
                    )
                }
            }
            Empty -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            Error -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        items(comments) { item ->
            CommentRow(comment = item, Modifier.fillMaxWidth())
        }
    }
}
