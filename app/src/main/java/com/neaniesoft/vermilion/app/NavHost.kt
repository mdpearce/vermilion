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
import com.neaniesoft.vermilion.posts.ui.PostsScreen

@ExperimentalMaterialNavigationApi
@ExperimentalPagingApi
@Composable
fun VermilionNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = VermilionScreen.Posts.name,
        modifier = modifier
    ) {
        composable(VermilionScreen.Posts.name) {
            PostsScreen(onOpenPostDetails = { postId ->
                navController.navigate("${VermilionScreen.PostDetails}/${postId.value}")
            }) { uri ->
                navController.navigate(customTabRoute(uri))
            }
        }

        composable(
            "${VermilionScreen.PostDetails.name}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            PostDetailsScreen() {
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
