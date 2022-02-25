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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.posts.ui.PostContent
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Composable
fun PostDetailsScreen(
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onOpenUri: (Uri) -> Unit,
) {
    val postDetailsState by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val columnState = rememberLazyListState()
    val initialScrollPosition = viewModel.restoredScrollPosition.collectAsState(
        initial = ScrollPosition(
            0,
            0
        )
    )
    val isScrolling by remember {
        derivedStateOf { columnState.isScrollInProgress }
    }

    val scrollPosition by remember {
        derivedStateOf {
            ScrollPosition(
                columnState.firstVisibleItemIndex,
                columnState.firstVisibleItemScrollOffset
            )
        }
    }

    if (!isScrolling) {
        LaunchedEffect(key1 = scrollPosition) {
            viewModel.onScrollStateUpdated(scrollPosition)
        }
    }

    // Only launch this effect if we have items
    LaunchedEffect(key1 = comments.count() > 0) {
        if (columnState.layoutInfo.totalItemsCount > 1) {
            columnState.scrollToItem(
                initialScrollPosition.value.index,
                initialScrollPosition.value.offset
            )
        }
    }

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
