package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.posts.ui.PostContent

@Composable
fun PostDetailsScreen(
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onOpenUri: (Uri) -> Unit,
) {
    val postDetailsState by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val columnState = rememberLazyListState()

    viewModel.onScrollStateUpdated(columnState.firstVisibleItemIndex, columnState.firstVisibleItemScrollOffset)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {

        LazyColumn(state = columnState) {
            when (val currentPostDetailsState = postDetailsState) {
                is PostDetails -> {
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                            PostContent(
                                post = currentPostDetailsState.post,
                                shouldTruncate = false,
                                shouldHideNsfw = false,
                                onMediaClicked = { onOpenUri(it.link.toString().toUri()) },
                                onSummaryClicked = {},
                                onCommunityClicked = {}
                            )
                        }
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
                when (item) {
                    is CommentKind.Full -> CommentRow(
                        comment = item.comment,
                        Modifier.fillMaxWidth()
                    )
                    is CommentKind.Stub -> MoreCommentsStubRow(
                        stub = item.stub,
                        Modifier.fillMaxWidth()
                    ) { viewModel.onMoreCommentsClicked(it) }
                }
            }
        }
    }
}
