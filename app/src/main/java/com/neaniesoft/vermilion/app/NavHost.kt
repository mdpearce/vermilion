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
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.postdetails.ui.PostDetailsScreen
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
        composable(
            VermilionScreen.Home.name
        ) {
            PostsScreen(
                community = com.neaniesoft.vermilion.coreentities.FrontPage,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { community ->
                    if (community is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${community.name.value}")
                    }
                }
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
                community = community,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { communityToOpen ->
                    if (communityToOpen is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${communityToOpen.name.value}")
                    }
                }
            )
        }

        // Post with comments
        composable(
            "${VermilionScreen.PostDetails.name}/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
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
