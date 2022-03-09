package com.neaniesoft.vermilion.app

import VermilionAppState
import android.os.Bundle
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
import com.neaniesoft.vermilion.accounts.adapters.driving.ui.UserAccountScreen
import com.neaniesoft.vermilion.app.customtabs.customTab
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.postdetails.ui.PostDetailsScreen
import com.neaniesoft.vermilion.posts.ui.PostsScreen
import com.neaniesoft.vermilion.ui.images.ImageDialog
import com.neaniesoft.vermilion.ui.videos.VideoDialog
import com.neaniesoft.vermilion.ui.videos.custom.youtube.YouTubeDialog
import com.neaniesoft.vermilion.ui.videos.direct.VideoDescriptor
import com.neaniesoft.vermilion.ui.videos.external.ExternalVideoDialog
import kotlinx.coroutines.FlowPreview
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder

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
                CommunityName(name), CommunityId("")
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
            "${VermilionScreen.PostDetails.name}/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            PostDetailsScreen(appState = appState) { route ->
                navController.navigate(route)
            }
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
            route = "${VermilionScreen.Video}/{videoDescriptor}",
            arguments = listOf(
                navArgument("videoDescriptor") { type = VideoDescriptorParamType }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) { backStackEntry ->
            val video =
                requireNotNull(backStackEntry.arguments?.getParcelable<VideoDescriptor>("videoDescriptor"))

            VideoDialog(videoDescriptor = video, onDismiss = {
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
    }
}

object VideoDescriptorParamType : NavType<VideoDescriptor>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): VideoDescriptor? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): VideoDescriptor {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: VideoDescriptor) {
        bundle.putParcelable(key, value)
    }
}
