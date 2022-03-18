package com.neaniesoft.vermilion.posts.ui

import VermilionAppState
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.utils.getLogger
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalPagingApi
@Composable
fun PostsScreen(
    appState: VermilionAppState,
    community: Community,
    shouldHideNsfw: Boolean,
    onRoute: (String) -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val logger by remember { derivedStateOf { getLogger("PostsScreen") } }
    val pagingItems = viewModel.pagingData(community.routeName).collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    val isScrolling by remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    val scrollPosition = remember {
        derivedStateOf {
            ScrollPosition(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
    }

    // Only launch this effect if we have items
    val loadedPosts by derivedStateOf { pagingItems.itemCount > 0 }

    LaunchedEffect(loadedPosts) {
        val loadedPosts = loadedPosts
        val scrollToPosition = viewModel.getSavedScrollPosition()
        if (loadedPosts && scrollToPosition != null) {
            logger.debugIfEnabled { "Posts are loaded, scroll position is not null: $scrollToPosition. Scrolling..." }
            listState.scrollToItem(
                scrollToPosition.index,
                scrollToPosition.offset
            )
        }
    }

    LaunchedEffect(isScrolling) {
        if (loadedPosts && !isScrolling) {
            logger.debugIfEnabled { "Posts are loaded, scrolling stopped" }
            viewModel.onScrollStateUpdated(scrollPosition.value)
        }
    }

    // Listen to taps on the app bar from the top level scaffold
    LaunchedEffect(key1 = Unit) {
        appState.appBarClicks.collect {
            listState.animateScrollToItem(0, 0)
        }
    }

    Box {
        PostsList(
            listState = listState,
            posts = pagingItems,
            shouldHideNsfw = shouldHideNsfw,
            onMediaClicked = { post ->
                viewModel.onMediaClicked(post)
            },
            onPostClicked = { post ->
                viewModel.onOpenPostDetails(post.id)
            },
            onCommunityClicked = { community -> viewModel.onOpenCommunity(community) },
            onUriClicked = { viewModel.onUriClicked(it) },
            onUpVoteClicked = { viewModel.onUpVoteClicked(it) },
            onDownVoteClicked = { viewModel.onDownVoteClicked(it) }
        )
    }
}

@Composable
fun PostsList(
    listState: LazyListState,
    posts: LazyPagingItems<Post>,
    shouldHideNsfw: Boolean,
    onPostClicked: (Post) -> Unit,
    onMediaClicked: (Post) -> Unit,
    onCommunityClicked: (Community) -> Unit,
    onUriClicked: (Uri) -> Unit,
    onUpVoteClicked: (Post) -> Unit,
    onDownVoteClicked: (Post) -> Unit
) {
    val isRefreshing = posts.loadState.refresh is LoadState.Loading
    val refreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    SwipeRefresh(state = refreshState, onRefresh = { posts.refresh() }) {
        LazyColumn(state = listState, modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
            items(posts) { post ->
                Box(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
                    if (post != null) {
                        PostCard(
                            post = post,
                            onClick = onPostClicked,
                            onMediaClicked = onMediaClicked,
                            onCommunityClicked = onCommunityClicked,
                            shouldHideNsfw = shouldHideNsfw,
                            onUriClicked = onUriClicked,
                            onUpVoteClicked = onUpVoteClicked,
                            onDownVoteClicked = onDownVoteClicked
                        )
                    } else {
                        PostCardPlaceholder()
                    }
                }
            }

            posts.apply {
                val refresh = loadState.refresh
                val append = loadState.append
                when {
                    refresh is LoadState.Loading -> {
                        item {
                            LoadingScreen(Modifier.fillParentMaxSize())
                        }
                    }
                    append is LoadState.Loading -> {
                        item {
                            LoadingItem()
                        }
                    }
                    refresh is LoadState.Error -> {
                        item {
                            ErrorItem(
                                modifier = Modifier.fillParentMaxSize(),
                                message = stringResource(id = R.string.error_loading_posts_message)
                            ) {
                                retry()
                            }
                        }
                    }
                    append is LoadState.Error -> {
                        item {
                            ErrorItem(message = stringResource(id = R.string.error_loading_posts_message)) {
                                retry()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingItem() {
    CircularProgressIndicator(
        modifier =
        Modifier
            .testTag("ProgressBarItem")
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentWidth(
                Alignment.CenterHorizontally
            )
    )
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            LoadingItem()
        }
    }
}

@Composable
fun ErrorItem(
    message: String,
    modifier: Modifier = Modifier,
    onClickRetry: () -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.error
        )
        OutlinedButton(onClick = onClickRetry) {
            Text(text = stringResource(id = R.string.error_retry))
        }
    }
}

@Preview(name = "Error item")
@Composable
fun ErrorItemPreview() {
    VermilionTheme {
        ErrorItem(message = "Error message") {
        }
    }
}

@Preview(name = "Loading screen")
@Composable
fun LoadingScreenPreview() {
    VermilionTheme {
        LoadingScreen()
    }
}
//
// @Preview(name = "Posts light theme")
// @Composable
// fun PostsScreenPreview() {
//     VermilionTheme {
//         PostsList(
//             flowOf(
//                 PagingData.from(
//                     listOf(
//                         DUMMY_TEXT_POST,
//                         DUMMY_TEXT_POST
//                     )
//                 )
//             ).collectAsLazyPagingItems(),
//             {}, {}
//         ) {}
//     }
// }
//
// @Preview(name = "Posts dark theme")
// @Composable
// fun PostsScreenPreviewDark() {
//     VermilionTheme(darkTheme = true) {
//         PostsList(
//             flowOf(
//                 PagingData.from(
//                     listOf(
//                         DUMMY_TEXT_POST,
//                         DUMMY_TEXT_POST
//                     )
//                 )
//             ).collectAsLazyPagingItems(),
//             {}, {}
//         ) {}
//     }
// }
