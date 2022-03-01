package com.neaniesoft.vermilion.posts.ui

import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalPagingApi
@Composable
fun PostsScreen(
    community: Community,
    onRoute: (String) -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.pagingData(community.routeName).collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val initialScrollPosition = viewModel.restoredScrollPosition.collectAsState(
        initial = null
    )
    val routeEvent by viewModel.routeEvents.collectAsState(initial = "")
    LaunchedEffect(key1 = routeEvent) {
        onRoute(routeEvent)
    }

    val isScrolling = remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    val scrollPosition = remember {
        derivedStateOf {
            ScrollPosition(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
    }

    if (!isScrolling.value) {
        LaunchedEffect(key1 = scrollPosition) {
            viewModel.onScrollStateUpdated(scrollPosition.value)
        }
    }

    // Only launch this effect if we have items
    LaunchedEffect(key1 = pagingItems.itemCount > 0) {
        val scrollToPosition = initialScrollPosition.value
        if (pagingItems.itemCount > 0 && scrollToPosition != null) {
            Log.d("PostsScreen", "Scrolling to $scrollToPosition")
            listState.scrollToItem(
                scrollToPosition.index,
                scrollToPosition.offset
            )
        }
    }

    Box {
        PostsList(
            listState = listState,
            posts = pagingItems,
            onMediaClicked = { post ->
                viewModel.onOpenUri(post, post.link.toString().toUri())
            },
            onPostClicked = { post ->
                viewModel.onOpenPostDetails(post.id)
            },
            onCommunityClicked = { community -> viewModel.onOpenCommunity(community) }
        )
    }
}

@Composable
fun PostsList(
    listState: LazyListState,
    posts: LazyPagingItems<Post>,
    onPostClicked: (Post) -> Unit,
    onMediaClicked: (Post) -> Unit,
    onCommunityClicked: (Community) -> Unit
) {
    LazyColumn(state = listState, modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        items(posts) { post ->
            Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                if (post != null) {
                    PostCard(post = post, onPostClicked, onMediaClicked, onCommunityClicked)
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
