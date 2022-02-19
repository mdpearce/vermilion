package com.neaniesoft.vermilion.app

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.neaniesoft.vermilion.tabs.adapters.driving.ui.ActiveTab
import com.neaniesoft.vermilion.tabs.adapters.driving.ui.TabBottomBar
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

        LaunchedEffect(key1 = Unit, block = {
            viewModel.routeEvents.collect { route ->
                if (!navController.popBackStack(route, false)) {
                    navController.navigate(route) {
                        popUpTo(VermilionScreen.Home.name) {
                            inclusive = route.startsWith(VermilionScreen.Home.name)
                        }
                    }
                }
            }
        })

        val activeTab by viewModel.activeTab.collectAsState()

        LaunchedEffect(key1 = activeTab, block = {
            viewModel.clearTabFromBackstackEvents.collect { clearTab ->
                @Suppress("UnnecessaryVariable")
                val currentTab = activeTab
                if (currentTab is ActiveTab.Tab && currentTab.id == clearTab.id) {
                    navController.popBackStack(VermilionScreen.Home.name, false)
                }
            }
        })


        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { scaffoldState.snackbarHostState },
            topBar = {
                TopAppBar(title = {
                    Text(currentScreen.name)
                })
            },
            bottomBar = {
                TabBottomBar(
                    tabs = tabs,
                    activeTab = activeTab,
                    onHomeButtonClicked = {
                        viewModel.onHomeButtonClicked()
                    },
                    onUserButtonClicked = {
                        viewModel.onUserButtonClicked()
                    },
                    onTabClicked = {
                        viewModel.onTabClicked(it)
                    },
                    onTabCloseClicked = {
                        viewModel.onTabCloseClicked(it)
                    })
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

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents: SharedFlow<String> = _routeEvents.asSharedFlow()

    private val _clearTabFromBackstackEvents = MutableSharedFlow<TabState>()
    val clearTabFromBackstackEvents = _clearTabFromBackstackEvents.asSharedFlow()

    private val _activeTab = MutableStateFlow<ActiveTab>(ActiveTab.None)
    val activeTab = _activeTab.asStateFlow()

    fun onNavigationEvent(destination: NavDestination, args: Bundle?) {
        val route = destination.route
        Log.d("VermilionAppViewModel", "Route: $route; args: $args")
        if (route != null) {
            when {
                route.startsWith(VermilionScreen.PostDetails.name) -> {
                    val id =
                        requireNotNull(args?.getString("id")) { "Received a post details route with no id" }
                    viewModelScope.launch(Dispatchers.IO) {
                        val tab = tabSupervisor.addNewPostDetailsTabIfNotExists(ParentId(id))
                        _activeTab.emit(ActiveTab.Tab(tab.id))
                    }
                }
                route.startsWith(VermilionScreen.Home.name) -> {
                    viewModelScope.launch { _activeTab.emit(ActiveTab.Home) }
                }
                else -> {
                    viewModelScope.launch { _activeTab.emit(ActiveTab.None) }
                }
            }
        }
    }

    fun onTabClicked(tab: TabState) {
        emitRouteEvent(tab.type, tab.parentId)
    }

    private fun emitRouteEvent(type: TabType, parentId: ParentId) {
        if (type == TabType.POST_DETAILS) {
            val route = "${VermilionScreen.PostDetails}/${parentId.value}"
            viewModelScope.launch { _routeEvents.emit(route) }
        }
    }

    fun onTabCloseClicked(tab: TabState) {
        viewModelScope.launch {
            tabSupervisor.removeTab(tab)
            _clearTabFromBackstackEvents.emit(tab)
        }
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch { _routeEvents.emit(VermilionScreen.Home.name) }
    }

    fun onUserButtonClicked() {
        viewModelScope.launch { _routeEvents.emit(VermilionScreen.MyAccount.name) }
    }
}
