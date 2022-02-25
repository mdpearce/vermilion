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
        composable(VermilionScreen.Home.name) {
            PostsScreen(
                community = FrontPage,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}/0")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { community ->
                    if (community is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${community.name.value}/0")
                    }
                }
            )
        }

        composable(
            "${VermilionScreen.Posts}/{communityName}/{initialScrollIndex}",
            arguments = listOf(
                navArgument("communityName") { type = NavType.StringType },
                navArgument("initialScrollIndex") { type = NavType.IntType }
            )
        ) {
            val name = it.arguments?.getString("communityName")
                ?: throw IllegalStateException("Cannot navigate to posts without a community name")
            val community = NamedCommunity(CommunityName(name))
            PostsScreen(
                community = community,
                onOpenPostDetails = { postId ->
                    navController.navigate("${VermilionScreen.PostDetails}/${postId.value}/0")
                },
                onOpenUri = { uri ->
                    navController.navigate(customTabRoute(uri))
                },
                onOpenCommunity = { communityToOpen ->
                    if (communityToOpen is NamedCommunity) {
                        navController.navigate("${VermilionScreen.Posts}/${communityToOpen.name.value}/0")
                    }
                }
            )
        }

        composable(
            "${VermilionScreen.PostDetails.name}/{id}/{initialScrollIndex}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("initialScrollIndex") { type = NavType.IntType }
            )
        ) {
            PostDetailsScreen {
                navController.navigate(customTabRoute(it))
            }
        }

        bottomSheet(VermilionScreen.MyAccount.name) {
            UserAccountScreen()
        }

        customTab(
            "${VermilionScreen.CustomTab.name}/{uri}",
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        )
    }
}
