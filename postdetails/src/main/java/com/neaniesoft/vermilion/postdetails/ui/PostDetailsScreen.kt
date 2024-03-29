package com.neaniesoft.vermilion.postdetails.ui

import VermilionAppState
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.postdetails.R
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.postdetails.domain.entities.ThreadStub
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.ui.DUMMY_TEXT_POST
import com.neaniesoft.vermilion.posts.ui.PostContent
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.utils.getLogger
import kotlinx.coroutines.FlowPreview

@ExperimentalFoundationApi
@FlowPreview
@Composable
fun PostDetailsScreen(
    appState: VermilionAppState,
    postId: PostId,
    onRoute: (String) -> Unit,
    postDetailsViewModel: PostDetailsViewModel = hiltViewModel(),
    postViewModel: PostViewModel = hiltViewModel(),
    commentsViewModel: CommentsViewModel = hiltViewModel()
) {
    val logger by remember { derivedStateOf { getLogger("PostDetailsScreen") } }

    val isRefreshing by commentsViewModel.networkIsActive.collectAsState(initial = false)

    LaunchedEffect(postId) {
        postViewModel.onPostId(postId)
        commentsViewModel.onPostId(postId)
    }

    val comments by commentsViewModel.comments.collectAsState()
    val postState by postViewModel.post.collectAsState()

    val columnState = rememberLazyListState()

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
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

    // Only launch this effect if we have items
    val commentsLoaded by derivedStateOf { comments.isNotEmpty() }
    LaunchedEffect(commentsLoaded) {
        val scrollToPosition = postDetailsViewModel.getSavedScrollPosition()
        if (commentsLoaded && scrollToPosition != null) {
            columnState.scrollToItem(
                scrollToPosition.index,
                scrollToPosition.offset
            )
        }
    }

    LaunchedEffect(isScrolling) {
        if (commentsLoaded && !isScrolling) {
            postDetailsViewModel.onScrollStateUpdated(scrollPosition)
        }
    }

    LaunchedEffect(Unit) {
        postDetailsViewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    LaunchedEffect(key1 = Unit) {
        commentsViewModel.scrollToEvents.collect {
            columnState.animateScrollToItem(it, 0)
        }
    }

    LaunchedEffect(key1 = Unit) {
        appState.appBarClicks.collect {
            columnState.animateScrollToItem(0, 0)
        }
    }

    PostDetailsScreenContent(
        swipeRefreshState = swipeRefreshState,
        onRefresh = { commentsViewModel.onRefresh(postId) },
        lazyListState = columnState,
        postState = postState,
        comments = comments,
        onOpenUri = { postDetailsViewModel.onOpenUri(it) },
        onUpVoteClicked = { postViewModel.onUpVoteClicked(it) },
        onDownVoteClicked = { postViewModel.onDownVoteClicked(it) },
        onMoreCommentsClicked = { commentsViewModel.onMoreCommentsClicked(it) },
        onThreadClicked = { postDetailsViewModel.onThreadClicked(it) },
        onCommentNavDownClicked = { commentsViewModel.onCommentNavDownClicked(it) },
        onCommentClicked = { commentsViewModel.onCommentClicked(it) },
        onCommentLongPressed = { commentsViewModel.onCommentLongPressed(it) },
        onCommentUpVoteClicked = { commentsViewModel.onCommentUpVoteClicked(it) },
        onCommentDownVoteClicked = { commentsViewModel.onCommentDownVoteClicked(it) }
    )
}

@ExperimentalFoundationApi
@Composable
fun PostDetailsScreenContent(
    swipeRefreshState: SwipeRefreshState,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    postState: PostState,
    comments: List<CommentKind>,
    onOpenUri: (Uri) -> Unit,
    onUpVoteClicked: (Post) -> Unit,
    onDownVoteClicked: (Post) -> Unit,
    onMoreCommentsClicked: (CommentStub) -> Unit,
    onThreadClicked: (ThreadStub) -> Unit,
    onCommentNavDownClicked: (Int) -> Unit,
    onCommentLongPressed: (Comment) -> Unit,
    onCommentClicked: (Comment) -> Unit,
    onCommentUpVoteClicked: (Comment) -> Unit,
    onCommentDownVoteClicked: (Comment) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.surface,
        elevation = 0.dp
    ) {
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { onRefresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(state = lazyListState) {
                item {
                    PostDetails(
                        postState = postState,
                        onOpenUri = onOpenUri,
                        onUpVoteClicked = onUpVoteClicked,
                        onDownVoteClicked = onDownVoteClicked
                    )
                }

                items(comments) { item ->
                    if (!item.isHidden) {
                        when (item) {
                            is CommentKind.Full -> CommentRow(
                                comment = item.comment,
                                Modifier.fillMaxWidth(),
                                onUriClicked = { onOpenUri(it.toUri()) },
                                onLongPress = onCommentLongPressed,
                                onClick = onCommentClicked,
                                onUpVoteClicked = onCommentUpVoteClicked,
                                onDownVoteClicked = onCommentDownVoteClicked
                            )
                            is CommentKind.Stub -> MoreCommentsStubRow(
                                stub = item.stub,
                                Modifier.fillMaxWidth(),
                                onClick = { onMoreCommentsClicked(it) }
                            )
                            is CommentKind.Thread -> ThreadStubRow(
                                stub = item.stub,
                                Modifier.fillMaxWidth(),
                                onClick = { onThreadClicked(it) }
                            )
                        }
                    }
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
                onClick = { onCommentNavDownClicked(lazyListState.firstVisibleItemIndex) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                    contentDescription = "Next top level comment"
                )
            }
        }
    }
}

@Composable
fun PostDetails(
    postState: PostState,
    onOpenUri: (Uri) -> Unit,
    onUpVoteClicked: (Post) -> Unit,
    onDownVoteClicked: (Post) -> Unit
) {
    when (val post = postState) {
        is PostState.Post -> {
            Surface(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                PostContent(
                    post = post.post,
                    shouldTruncate = false,
                    shouldHideNsfw = false,
                    onMediaClicked = { onOpenUri(it.link) },
                    onSummaryClicked = {},
                    onCommunityClicked = {},
                    onUriClicked = { onOpenUri(it) },
                    onUpVoteClicked = { onUpVoteClicked(it) },
                    onDownVoteClicked = { onDownVoteClicked(it) }
                )
            }
        }
        PostState.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        PostState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
fun PostDetailsScreenDark() {
    VermilionTheme(darkTheme = true) {
        PostDetailsScreenContent(
            postState = PostState.Post(DUMMY_TEXT_POST),
            comments = listOf(
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT),
                CommentKind.Full(DUMMY_COMMENT)
            ),
            lazyListState = rememberLazyListState(),
            swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
            onRefresh = {},
            onOpenUri = {},
            onMoreCommentsClicked = {},
            onCommentNavDownClicked = {},
            onUpVoteClicked = {},
            onDownVoteClicked = {},
            onThreadClicked = {},
            onCommentLongPressed = {},
            onCommentClicked = {},
            onCommentUpVoteClicked = {},
            onCommentDownVoteClicked = {}
        )
    }
}
