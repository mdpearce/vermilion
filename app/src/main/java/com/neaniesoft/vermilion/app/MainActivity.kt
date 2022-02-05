package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neaniesoft.vermilion.posts.adapters.driving.ui.PostsScreen
import com.neaniesoft.vermilion.ui.theme.Vermilion700
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VermilionApp()
        }
    }
}

@Composable
fun VermilionApp() {
    VermilionTheme {
        val navController = rememberNavController()
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = VermilionScreen.fromRoute(backStackEntry.value?.destination?.route)

        val scaffoldState = rememberScaffoldState()

        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { scaffoldState.snackbarHostState },
            topBar = {
                TopAppBar(title = {
                    Text(currentScreen.name)
                })
            },
            bottomBar = {
                BottomAppBar {

                }
            }
        ) { innerPadding ->
            VermilionNavHost(navController, Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun VermilionNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = VermilionScreen.Posts.name, modifier = modifier) {
        composable(VermilionScreen.Posts.name) {
            PostsScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VermilionAppPreview() {
    VermilionApp()
}