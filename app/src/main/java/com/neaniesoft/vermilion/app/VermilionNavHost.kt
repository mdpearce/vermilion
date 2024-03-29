package com.neaniesoft.vermilion.app

import VermilionAppState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.pager.ExperimentalPagerApi
import com.neaniesoft.vermilion.accounts.adapters.driving.ui.UserAccountScreen
import com.neaniesoft.vermilion.app.customtabs.customTab
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.ui.CommentThreadScreen
import com.neaniesoft.vermilion.postdetails.ui.PostDetailsScreen
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.ui.PostGalleryDialog
import com.neaniesoft.vermilion.posts.ui.PostsScreen
import com.neaniesoft.vermilion.ui.images.ImageDialog
import com.neaniesoft.vermilion.ui.videos.custom.youtube.YouTubeDialog
import com.neaniesoft.vermilion.ui.videos.direct.VideoDialog
import com.neaniesoft.vermilion.ui.videos.external.ExternalVideoDialog
import kotlinx.coroutines.FlowPreview
import java.net.URLDecoder

@ExperimentalPagerApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@FlowPreview
@ExperimentalMaterialNavigationApi
@ExperimentalPagingApi
@Composable
fun VermilionNavHost(
    navController: NavHostController,
    appState: VermilionAppState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = VermilionScreen.Home.name,
        modifier = modifier
    ) {
        // Home screen - this is just an instance of PostsScreen hardcoded to the front page
        composable(
            VermilionScreen.Home.name
        ) {
            PostsScreen(
                appState = appState,
                community = com.neaniesoft.vermilion.coreentities.FrontPage,
                onRoute = { route ->
                    if (route.isNotEmpty()) {
                        navController.navigate(route)
                    }
                },
                shouldHideNsfw = true
            )
        }

        // Individual subreddit listings
        composable(
            "${VermilionScreen.Posts}/{communityName}",
            arguments = listOf(
                navArgument("communityName") { type = NavType.StringType }
            )
        ) {
            val name = it.arguments?.getString("communityName")
                ?: throw IllegalStateException("Cannot navigate to posts without a community name")
            val community = NamedCommunity(
                CommunityName(name),
                CommunityId("")
            )
            PostsScreen(
                appState = appState,
                community = community,
                onRoute = { route ->
                    if (route.isNotEmpty()) {
                        navController.navigate(route)
                    }
                },
                shouldHideNsfw = false
            )
        }

        // Post with comments
        composable(
            "${VermilionScreen.PostDetails.name}/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) {
            val postId = PostId(requireNotNull(it.arguments?.getString("postId")))
            PostDetailsScreen(postId = postId, appState = appState, onRoute = { route ->
                navController.navigate(route)
            })
        }

        // Post with individual comment thread
        composable(
            "${VermilionScreen.CommentThread.name}/{postId}/{commentId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("commentId") { type = NavType.StringType }
            )
        ) {
            val postId = PostId(requireNotNull(it.arguments?.getString("postId")))
            val commentId = CommentId(requireNotNull(it.arguments?.getString("commentId")))
            CommentThreadScreen(
                postId = postId,
                commentId = commentId,
                appState = appState,
                onRoute = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Account/Settings
        bottomSheet(VermilionScreen.MyAccount.name) {
            UserAccountScreen()
        }

        // Custom tabs to open links
        customTab(
            "${VermilionScreen.CustomTab.name}/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        )

        // Fullscreen image viewer
        dialog(
            "${VermilionScreen.Image}/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType }),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val decodedUri = URLDecoder.decode(
                requireNotNull(backStackEntry.arguments?.getString(("uri"))),
                "utf-8"
            )
            ImageDialog(
                imageUri = decodedUri.toUri(),
                onDismiss = {
                    navController.popBackStack(backStackEntry.destination.id, true)
                }
            )
        }

        // Fullscreen video viewer
        dialog(
            route = "${VermilionScreen.Video}/{uri}",
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val uri =
                requireNotNull(backStackEntry.arguments?.getString("uri")).toUri()

            VideoDialog(uri, onDismiss = {
                navController.popBackStack(backStackEntry.destination.id, true)
            })
        }

        // Fullscreen external video viewer
        dialog(
            route = "${VermilionScreen.ExternalVideo}/{uri}",
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val uri = requireNotNull(backStackEntry.arguments?.getString("uri")).toUri()

            ExternalVideoDialog(uri, onDismiss = {
                navController.popBackStack(backStackEntry.destination.id, true)
            })
        }

        // Fullscreen youtube viewer
        dialog(
            route = "${VermilionScreen.YouTube}/{videoId}",
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val videoId =
                requireNotNull(backStackEntry.arguments?.getString("videoId"))

            YouTubeDialog(videoId, onDismiss = {
                navController.popBackStack(backStackEntry.destination.id, true)
            })
        }

        // Fullscreen image gallery
        dialog(
            route = "${VermilionScreen.ImageGallery}/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val postId = requireNotNull(backStackEntry.arguments?.getString("postId"))

            PostGalleryDialog(postId = PostId(postId), onDismiss = {
                navController.popBackStack(backStackEntry.destination.id, true)
            })
        }
    }
}
