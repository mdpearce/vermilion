package com.neaniesoft.vermilion.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.neaniesoft.vermilion.app.customtabs.CustomTabNavigator
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import java.time.Clock

@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@Composable
fun VermilionApp(clock: Clock) {
    VermilionTheme {
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val context = LocalContext.current
        val customTabNavigator = remember {
            CustomTabNavigator(context, clock)
        }
        navController.navigatorProvider += bottomSheetNavigator
        navController.navigatorProvider += customTabNavigator
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
                    BottomNavBar { navController.navigate(VermilionScreen.MyAccount.name) }
                }
            }
        ) { innerPadding ->
            ModalBottomSheetLayout(bottomSheetNavigator) {
                VermilionNavHost(navController, Modifier.padding(innerPadding))
            }
        }
    }
}
