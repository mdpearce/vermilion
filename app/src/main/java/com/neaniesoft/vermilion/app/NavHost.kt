package com.neaniesoft.vermilion.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.neaniesoft.vermilion.accounts.adapters.driving.ui.UserAccountScreen
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
            PostsScreen()
        }

        bottomSheet(VermilionScreen.MyAccount.name) {
            UserAccountScreen()
        }
    }
}
