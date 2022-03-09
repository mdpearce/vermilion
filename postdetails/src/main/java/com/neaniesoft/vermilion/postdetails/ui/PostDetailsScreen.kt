package com.neaniesoft.vermilion.postdetails.ui

import VermilionAppState
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.neaniesoft.vermilion.postdetails.R
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.ui.DUMMY_TEXT_POST
import com.neaniesoft.vermilion.posts.ui.PostContent
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Composable
fun PostDetailsScreen(
    appState: VermilionAppState,
    viewModel: PostDetailsViewModel = hiltViewModel(),
    onRoute: (String) -> Unit,
) {
    val postDetailsState by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val columnState = rememberLazyListState()
    val initialScrollPosition = viewModel.restoredScrollPosition.collectAsState(
        initial = null
    )
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val isScrolling by remember {
        derivedStateOf { columnState.isScrollInProgress }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.scrollToEvents.collect {
            columnState.animateScrollToItem(it, 0)
        }
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
        val scrollToPosition = initialScrollPosition.value
        if (comments.count() > 0 && scrollToPosition != null) {
            columnState.scrollToItem(
                scrollToPosition.index,
                scrollToPosition.offset
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        appState.appBarClicks.collect {
            columnState.animateScrollToItem(0, 0)
        }
    }

    PostDetailsScreenContent(
        postDetailsState = postDetailsState,
        comments = comments,
        listState = columnState,
        swipeRefreshState = swipeRefreshState,
        onRefresh = { viewModel.refresh() },
        onMediaClicked = { viewModel.onOpenUri(it.link) },
        onUriClicked = { viewModel.onOpenUri(it) },
        onMoreCommentsClicked = { viewModel.onMoreCommentsClicked(it) },
        onCommentNavDownClicked = { viewModel.onCommentNavDownClicked(columnState.firstVisibleItemIndex) }
    )
}

@Composable
fun PostDetailsScreenContent(
    postDetailsState: PostDetailsState,
    comments: List<CommentKind>,
    listState: LazyListState,
    swipeRefreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    onMediaClicked: (Post) -> Unit,
    onUriClicked: (Uri) -> Unit,
    onMoreCommentsClicked: (CommentStub) -> Unit,
    onCommentNavDownClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(state = listState) {
                when (postDetailsState) {
                    is PostDetails -> {
                        item {
                            Surface(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                PostContent(
                                    post = postDetailsState.post,
                                    shouldTruncate = false,
                                    shouldHideNsfw = false,
                                    onMediaClicked = onMediaClicked,
                                    onSummaryClicked = {},
                                    onCommunityClicked = {},
                                    onUriClicked = onUriClicked
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
                            Modifier.fillMaxWidth(),
                            onUriClicked = { onUriClicked(it.toUri()) }
                        )
                        is CommentKind.Stub -> MoreCommentsStubRow(
                            stub = item.stub,
                            Modifier.fillMaxWidth(),
                            onClick = onMoreCommentsClicked
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 8.dp, end = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onCommentNavDownClicked
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                    contentDescription = "Next top level comment"
                )
            }
        }
    }
}

@Preview
@Composable
fun PostDetailsScreenDark() {
    VermilionTheme(darkTheme = true) {
        PostDetailsScreenContent(
            postDetailsState = PostDetails(DUMMY_TEXT_POST),
            comments = listOf(
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
            ),
            listState = rememberLazyListState(),
            swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
            onRefresh = { /*TODO*/ },
            onMediaClicked = {},
            onUriClicked = {},
            onMoreCommentsClicked = {},
            onCommentNavDownClicked = {}
        )
    }
}
