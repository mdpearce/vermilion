package com.neaniesoft.vermilion.postdetails.ui

import VermilionAppState
import android.net.Uri
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.neaniesoft.vermilion.postdetails.R
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.PostVotingService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.ui.DUMMY_TEXT_POST
import com.neaniesoft.vermilion.posts.ui.PostContent
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.utils.CoroutinesModule
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

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
    val isRefreshing by commentsViewModel.networkIsActive.collectAsState(initial = false)

    LaunchedEffect(postId) {
        postViewModel.onPostId(postId)
        commentsViewModel.onPostId(postId)
    }

    val comments by commentsViewModel.comments.collectAsState()
    val postState by postViewModel.post.collectAsState()

    val columnState = rememberLazyListState()
    val initialScrollPosition = postDetailsViewModel.restoredScrollPosition.collectAsState(
        initial = null
    )
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    val isScrolling by remember {
        derivedStateOf { columnState.isScrollInProgress }
    }

    LaunchedEffect(Unit) {
        postViewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    LaunchedEffect(Unit) {
        commentsViewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    LaunchedEffect(key1 = Unit) {
        commentsViewModel.scrollToEvents.collect {
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
            postDetailsViewModel.onScrollStateUpdated(scrollPosition)
        }
    }

    val commentsLoaded by derivedStateOf { comments.isNotEmpty() }

    // Only launch this effect if we have items
    LaunchedEffect(commentsLoaded) {
        val scrollToPosition = initialScrollPosition.value
        if (commentsLoaded && scrollToPosition != null) {
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
        swipeRefreshState = swipeRefreshState,
        onRefresh = { commentsViewModel.onRefresh(postId) },
        lazyListState = columnState,
        postState = postState,
        comments = comments,
        onOpenUri = { postDetailsViewModel.onOpenUri(it) },
        onUpVoteClicked = { postViewModel.onUpVoteClicked(it) },
        onDownVoteClicked = { postViewModel.onDownVoteClicked(it) },
        onMoreCommentsClicked = { commentsViewModel.onMoreCommentsClicked(it) },
        onCommentNavDownClicked = { commentsViewModel.onCommentNavDownClicked(it) }
    )

    // Surface(
    //     modifier = Modifier.fillMaxWidth(),
    //     color = MaterialTheme.colors.surface,
    //     elevation = 0.dp
    // ) {
    //     SwipeRefresh(
    //         state = swipeRefreshState,
    //         onRefresh = { commentsViewModel.onRefresh(postId) },
    //         modifier = Modifier.fillMaxSize()
    //     ) {
    //         LazyColumn(state = columnState) {
    //             item {
    //                 PostDetails(
    //                     viewModel = postViewModel
    //                 )
    //             }
    //
    //             items(comments) { item ->
    //                 when (item) {
    //                     is CommentKind.Full -> CommentRow(
    //                         comment = item.comment,
    //                         Modifier.fillMaxWidth(),
    //                         onUriClicked = { commentsViewModel.onOpenUri(it.toUri()) }
    //                     )
    //                     is CommentKind.Stub -> MoreCommentsStubRow(
    //                         stub = item.stub,
    //                         Modifier.fillMaxWidth(),
    //                         onClick = { commentsViewModel.onMoreCommentsClicked(it) }
    //                     )
    //                 }
    //             }
    //
    //         }
    //     }
    //     Box(
    //         Modifier
    //             .fillMaxSize()
    //             .padding(bottom = 8.dp, end = 8.dp),
    //         contentAlignment = Alignment.BottomEnd
    //     ) {
    //         FloatingActionButton(
    //             onClick = { commentsViewModel.onCommentNavDownClicked(columnState.firstVisibleItemIndex) }
    //         ) {
    //             Icon(
    //                 painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
    //                 contentDescription = "Next top level comment"
    //             )
    //         }
    //     }
    // }
}

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
    onCommentNavDownClicked: (Int) -> Unit
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
                    when (item) {
                        is CommentKind.Full -> CommentRow(
                            comment = item.comment,
                            Modifier.fillMaxWidth(),
                            onUriClicked = { onOpenUri(it.toUri()) }
                        )
                        is CommentKind.Stub -> MoreCommentsStubRow(
                            stub = item.stub,
                            Modifier.fillMaxWidth(),
                            onClick = { onMoreCommentsClicked(it) }
                        )
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

sealed class PostState {
    object Empty : PostState()
    object Error : PostState()
    data class Post(val post: com.neaniesoft.vermilion.posts.domain.entities.Post) : PostState()
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val linkRouter: LinkRouter,
    private val postVotingService: PostVotingService,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _routeEvents: MutableSharedFlow<String> = MutableSharedFlow()
    val routeEvents = _routeEvents.asSharedFlow()

    private val _post: MutableStateFlow<PostState> = MutableStateFlow(PostState.Empty)
    val post = _post.asStateFlow()

    fun onPostId(postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                postRepository.postFlow(postId).collect {
                    _post.emit(PostState.Post(it))
                }
            }
        }
    }

    fun onOpenUri(uri: Uri) {
        val route = linkRouter.routeForLink(uri)

        viewModelScope.launch { _routeEvents.emit(route) }
    }

    fun onUpVoteClicked(post: Post) {
        viewModelScope.launch {
            postVotingService.toggleUpVote(post)
        }
    }

    fun onDownVoteClicked(post: Post) {
        viewModelScope.launch {
            postVotingService.toggleDownVote(post)
        }
    }
}

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val linkRouter: LinkRouter,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val logger by logger()

    private val _comments: MutableStateFlow<List<CommentKind>> = MutableStateFlow(emptyList())
    val comments = _comments.asStateFlow()

    private val networkActivityIdentifier = UUID.randomUUID().toString()

    val networkIsActive = commentRepository.networkActivityUpdates.filter {
        it.identifier == networkActivityIdentifier
    }.map { it.isActive }

    private val _scrollToEvents = MutableSharedFlow<Int>()
    val scrollToEvents = _scrollToEvents.asSharedFlow()

    private val _routeEvents: MutableSharedFlow<String> = MutableSharedFlow()
    val routeEvents = _routeEvents.asSharedFlow()

    suspend fun onPostId(postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                commentRepository.getCommentsForPost(postId).collect { comments ->
                    _comments.emit(comments)
                }
            }
        }
        commentRepository.refreshIfRequired(
            postId,
            forceRefresh = false,
            networkActivityIdentifier = networkActivityIdentifier
        )
    }

    fun onMoreCommentsClicked(stub: CommentStub) {
        viewModelScope.launch {
            val comments: List<CommentKind> = commentRepository.fetchAndInsertMoreCommentsFor(stub)

            _comments.emit(comments)
        }
    }

    fun onCommentNavDownClicked(firstVisibleItemIndex: Int) {
        viewModelScope.launch {
            val foundIndex = comments.value.let { list ->
                list.subList(firstVisibleItemIndex, list.size)
                    .indexOfFirst { (it as? CommentKind.Full)?.comment?.depth == CommentDepth(0) }
            }
            if (foundIndex != -1) {
                val positionInCommentList = firstVisibleItemIndex + foundIndex + 1
                _scrollToEvents.emit(positionInCommentList)
            }
        }
    }

    fun onOpenUri(uri: Uri) {
        val route = linkRouter.routeForLink(uri)

        viewModelScope.launch { _routeEvents.emit(route) }
    }

    fun onRefresh(postId: PostId) {
        viewModelScope.launch {
            commentRepository.refreshIfRequired(
                postId,
                forceRefresh = true,
                networkActivityIdentifier = networkActivityIdentifier
            )
        }
    }
}

//
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
                CommentKind.Full(DUMMY_COMMENT),
            ),
            lazyListState = rememberLazyListState(),
            swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false),
            onRefresh = {},
            onOpenUri = {},
            onMoreCommentsClicked = {},
            onCommentNavDownClicked = {},
            onUpVoteClicked = {},
            onDownVoteClicked = {}
        )
    }
}
