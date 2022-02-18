package com.neaniesoft.vermilion.app

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.neaniesoft.vermilion.app.customtabs.CustomTabNavigator
import com.neaniesoft.vermilion.tabs.adapters.driving.ui.TabBottomBar
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@Composable
fun VermilionApp(
    clock: Clock,
    viewModel: VermilionAppViewModel = hiltViewModel()
) {
    VermilionTheme {
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val context = LocalContext.current
        val customTabNavigator = remember {
            CustomTabNavigator(context, clock)
        }
        navController.navigatorProvider += bottomSheetNavigator
        navController.navigatorProvider += customTabNavigator

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            viewModel.onNavigationEvent(destination, arguments)
        }

        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = VermilionScreen.fromRoute(backStackEntry.value?.destination?.route)

        val scaffoldState = rememberScaffoldState()
        val tabs by viewModel.tabs.collectAsState()

        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { scaffoldState.snackbarHostState },
            topBar = {
                TopAppBar(title = {
                    Text(currentScreen.name)
                })
            },
            bottomBar = {
                TabBottomBar(tabs = tabs, onUserButtonClicked = { /*TODO*/ }, onTabClicked = {})
            }
        ) { innerPadding ->
            ModalBottomSheetLayout(bottomSheetNavigator) {
                VermilionNavHost(navController, Modifier.padding(innerPadding))
            }
        }
    }
}

@HiltViewModel
class VermilionAppViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor
) : ViewModel() {
    val tabs = tabSupervisor.currentTabs

    fun onNavigationEvent(destination: NavDestination, args: Bundle?) {
        val route = destination.route
        Log.d("VermilionAppViewModel", "Route: $route; args: $args")
        if (route != null) {
            if (route.startsWith(VermilionScreen.PostDetails.name)) {
                val id =
                    requireNotNull(args?.getString("id")) { "Received a post details route with no id" }
                viewModelScope.launch(Dispatchers.IO) {
                    tabSupervisor.addNewPostDetailsTabIfNotExists(ParentId(id), DisplayName(id))
                }
            }
        }
    }
}
