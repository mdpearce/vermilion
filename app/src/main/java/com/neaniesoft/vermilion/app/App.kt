package com.neaniesoft.vermilion.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

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
