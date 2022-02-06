package com.neaniesoft.vermilion.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neaniesoft.vermilion.posts.adapters.driving.ui.PostsScreen


@Composable
fun VermilionNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = VermilionScreen.Posts.name, modifier = modifier) {
        composable(VermilionScreen.Posts.name) {
            PostsScreen()
        }
    }
}