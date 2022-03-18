package com.neaniesoft.vermilion.postdetails.ui

import VermilionAppState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.posts.domain.entities.PostId

@ExperimentalFoundationApi
@Composable
fun CommentThreadScreen(
    appState: VermilionAppState,
    postId: PostId,
    commentId: CommentId,
    onRoute: (String) -> Unit,
    postDetailsViewModel: PostDetailsViewModel = hiltViewModel(),
    postViewModel: PostViewModel = hiltViewModel(),
    commentsViewModel: CommentsViewModel = hiltViewModel()
) {
    val isRefreshing by commentsViewModel.networkIsActive.collectAsState(initial = false)

    LaunchedEffect(postId) {
        postViewModel.onPostId(postId)
        commentsViewModel.onCommentId(commentId, postId)
    }

    val comments by commentsViewModel.comments.collectAsState()
    val postState by postViewModel.post.collectAsState()

    val columnState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

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
        onCommentNavDownClicked = { commentsViewModel.onCommentNavDownClicked(it) },
        onThreadClicked = { postDetailsViewModel.onThreadClicked(it) },
        onCommentClicked = { commentsViewModel.onCommentClicked(it) },
        onCommentLongPressed = { commentsViewModel.onCommentLongPressed(it) },
        onCommentUpVoteClicked = { commentsViewModel.onCommentUpVoteClicked(it) },
        onCommentDownVoteClicked = { commentsViewModel.onCommentDownVoteClicked(it) },
    )
}
