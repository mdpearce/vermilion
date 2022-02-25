package com.neaniesoft.vermilion.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.neaniesoft.vermilion.accounts.adapters.driving.ui.UserAccountScreen
import com.neaniesoft.vermilion.app.customtabs.customTab
import com.neaniesoft.vermilion.app.customtabs.customTabRoute
import com.neaniesoft.vermilion.postdetails.ui.PostDetailsScreen
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.ui.PostsScreen

@ExperimentalMaterialNavigationApi
@ExperimentalPagingApi
@Composable
fun VermilionNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = VermilionScreen.Home.name,
        modifier = modifier
    ) {
        // Home screen - this is just an instance of PostsScreen hardcoded to the front page
        composable("${VermilionScreen.Home.name}/{initialScrollIndex/{initialScrollOffset}") {
            PostsScreen(
                community = FrontPage,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}/0/0")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { community ->
                    if (community is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${community.name.value}/0/0")
                    }
                }
            )
        }

        // Individual subreddit listings
        composable(
            "${VermilionScreen.Posts}/{communityName}/{initialScrollIndex}/{initialScrollOffset}",
            arguments = listOf(
                navArgument("communityName") { type = NavType.StringType },
                navArgument("initialScrollIndex") { type = NavType.IntType },
                navArgument("initialScrollOffset") { type = NavType.IntType }
            )
        ) {
            val name = it.arguments?.getString("communityName")
                ?: throw IllegalStateException("Cannot navigate to posts without a community name")
            val community = NamedCommunity(CommunityName(name))
            PostsScreen(
                community = community,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}/0/0")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { communityToOpen ->
                    if (communityToOpen is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${communityToOpen.name.value}/0/0")
                    }
                }
            )
        }

        // Post with comments
        composable(
            "${VermilionScreen.PostDetails.name}/{id}/{initialScrollIndex}/{initialScrollOffset}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("initialScrollIndex") { type = NavType.IntType },
                navArgument("initialScrollOffset") { type = NavType.IntType }
            )
        ) {
            PostDetailsScreen {
                navController.navigate(customTabRoute(it))
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
    }
}
