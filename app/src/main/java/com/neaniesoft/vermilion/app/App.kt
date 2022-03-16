package com.neaniesoft.vermilion.app

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.neaniesoft.vermilion.accounts.domain.UserAccountService
import com.neaniesoft.vermilion.app.customtabs.CustomTabNavigator
import com.neaniesoft.vermilion.communities.ui.CommunityList
import com.neaniesoft.vermilion.tabs.adapters.driving.ui.TabBottomBar
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import rememberVermilionAppState
import java.time.Clock

@FlowPreview
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalPagingApi
@ExperimentalMaterialNavigationApi
@Composable
fun VermilionApp(
    clock: Clock,
    userAccountService: UserAccountService,
    viewModel: VermilionAppViewModel = hiltViewModel()
) {
    VermilionTheme {
        val currentUser by userAccountService.currentUserAccount.collectAsState()

        // TODO Encapsulate this check somewhere else
        val isAuthorized = userAccountService.isAuthorized()
        if (currentUser != null && !isAuthorized) {
            Log.d("VermilionApp", "currentUser: $currentUser, isAuthenticated: $isAuthorized")
            userAccountService.logout()
        }

        val appState = rememberVermilionAppState()
        val navController = rememberNavController()
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val context = LocalContext.current
        val customTabNavigator = remember {
            CustomTabNavigator(context, clock)
        }
        navController.navigatorProvider += bottomSheetNavigator
        navController.navigatorProvider += customTabNavigator
        val destinationChangedListener: NavController.OnDestinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, arguments ->
                viewModel.onNavigationEvent(
                    destination,
                    arguments
                )
            }

        LaunchedEffect(Unit) {
            navController.addOnDestinationChangedListener(destinationChangedListener)
        }

        DisposableEffect(Unit) {
            onDispose {
                navController.removeOnDestinationChangedListener(destinationChangedListener)
            }
        }

        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = VermilionScreen.fromRoute(backStackEntry.value?.destination?.route)

        val scaffoldState = rememberScaffoldState()

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

        LaunchedEffect(Unit) {
            viewModel.currentTabRemovedEvents.collect {
                navController.popBackStack()
            }
        }

        LaunchedEffect(key1 = currentUser) {
            viewModel.onUserChanged(currentUser)
        }

        val subscribedCommunities by
        viewModel.subscribedCommunities.collectAsState(initial = emptyList())

        val scope = rememberCoroutineScope()

        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = { scaffoldState.snackbarHostState },
            topBar = {
                TopAppBar(
                    elevation = 16.dp,
                    title = { Text(currentScreen.name) },
                    modifier = Modifier.clickable { scope.launch { appState.onAppBarClicked() } },
                    actions = {
                        IconButton(onClick = { viewModel.onUserButtonClicked() }) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = stringResource(id = R.string.content_description_my_account_button)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                TabBottomBar(
                    onRoute = { route ->
                        navController.navigate(route)
                    }
                )
            },
            drawerContent = {
                CommunityList(
                    communities = subscribedCommunities,
                    onCommunityClicked = { viewModel.onCommunityClicked(it) }
                )
            }
        ) { innerPadding ->
            ModalBottomSheetLayout(bottomSheetNavigator) {
                VermilionNavHost(navController, appState, Modifier.padding(innerPadding))
            }
        }
    }
}
