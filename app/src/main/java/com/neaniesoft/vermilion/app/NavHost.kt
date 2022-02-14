package com.neaniesoft.vermilion.app

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.get
import androidx.navigation.navArgument
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.neaniesoft.vermilion.accounts.adapters.driving.ui.UserAccountScreen
import com.neaniesoft.vermilion.app.customtabs.CustomTabNavigator
import com.neaniesoft.vermilion.posts.ui.PostsScreen
import java.net.URLEncoder

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
            PostsScreen { uri ->
                navController.navigate(customTabRoute(uri))
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

fun NavGraphBuilder.customTab(
    route: String, arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList()
) {
    addDestination(CustomTabNavigator.Destination(provider[CustomTabNavigator::class])
        .apply {
            this.route = route
            arguments.forEach { (argName, argument) ->
                addArgument(argName, argument)
            }
            deepLinks.forEach { deepLink ->
                addDeepLink(deepLink)
            }
        }
    )
}

fun customTabRoute(uri: Uri): String =
    VermilionScreen.CustomTab.name + "/" + URLEncoder.encode(uri.toString(), "utf-8")
