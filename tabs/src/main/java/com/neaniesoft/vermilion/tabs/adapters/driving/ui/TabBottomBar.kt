package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.tabs.R
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.uistate.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@Composable
fun TabBottomBar(
    onRoute: (String) -> Unit,
    viewModel: TabBottomBarViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.routeEvents.collect {
            onRoute(it)
        }
    }

    BottomAppBar(elevation = 16.dp) {
        val tabs by viewModel.tabs.collectAsState(initial = emptyList())
        val activeTab by viewModel.activeTab.collectAsState(initial = null)
        val activeTabIndex = tabs.indexOfFirst {
            Log.d("TabBottomBar", "comparing ${it.id} to ${activeTab?.id}")
            it.id == activeTab?.id
        } + 1 // if none is found, we'll get -1, which will then map to 0 - Home.
        Log.d("TabBottomBar", "activeTab: $activeTab, activeTabIndex: $activeTabIndex")

        TabBottomBarContent(
            tabs = tabs,
            activeTab = activeTabIndex,
            onHomeButtonClicked = { viewModel.onHomeClicked() },
            onTabClicked = { viewModel.onTabClicked(it) },
            onTabCloseClicked = { viewModel.onTabCloseClicked(it) }
        )
    }
}

@Composable
fun TabBottomBarContent(
    tabs: List<TabState>,
    activeTab: Int,
    onHomeButtonClicked: () -> Unit,
    onTabClicked: (TabState) -> Unit,
    onTabCloseClicked: (TabState) -> Unit,
) {
    ScrollableTabRow(selectedTabIndex = activeTab) {
        LeadingIconTab(
            selected = activeTab == 0,
            onClick = onHomeButtonClicked,
            text = { Text(text = stringResource(id = R.string.content_description_home_button)) },
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = stringResource(id = R.string.content_description_home_button)
                )
            }
        )
        tabs.forEachIndexed { index, tabState ->

            Tab(selected = activeTab == index + 1,
                onClick = {
                    onTabClicked(tabState)
                },
                content = {

                    val interactionSource = remember { MutableInteractionSource() }

                    Row(
                        Modifier
                            .indication(interactionSource, LocalIndication.current)
                            .padding(horizontal = 16.dp)
                            .height(48.dp)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        val press = PressInteraction.Press(offset)
                                        interactionSource.emit(press)

                                        tryAwaitRelease()

                                        interactionSource.emit(PressInteraction.Release(press))
                                    },

                                    onTap = {
                                        onTabClicked(tabState)
                                    },

                                    onLongPress = {
                                        onTabCloseClicked(tabState)
                                    })
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = tabState.displayName.value,
                            style = MaterialTheme.typography.button.copy(textAlign = TextAlign.Center)
                        )
                    }
                }
            )
        }
    }
}

@HiltViewModel
class TabBottomBarViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor
) : ViewModel() {
    val tabs = tabSupervisor.currentTabs

    val activeTab = tabSupervisor.activeTab

    private val _routeEvents: MutableSharedFlow<String> = MutableSharedFlow()
    val routeEvents = _routeEvents.asSharedFlow()

    fun onTabClicked(tabState: TabState) {
        viewModelScope.launch {
            tabSupervisor.setActiveTab(tabState.type, tabState.parentId.value)
            val route = when (tabState.type) {
                TabType.HOME -> tabState.parentId.value
                TabType.POSTS -> "Posts/${tabState.parentId.value}"
                TabType.POST_DETAILS -> "PostDetails/${tabState.parentId.value}"
            }
            _routeEvents.emit(route)
        }
    }

    fun onHomeClicked() {
        viewModelScope.launch {
            tabSupervisor.setActiveTab(TabType.HOME, "Home")
            _routeEvents.emit("Home")
        }
    }

    fun onTabCloseClicked(tabState: TabState) {
        viewModelScope.launch { tabSupervisor.removeTab(tabState) }
    }
}

@Composable
fun TopLevelTab(
    isActive: Boolean,
    tabState: TabState,
    onTabClicked: (TabState) -> Unit,
    onCloseClicked: (TabState) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(end = 4.dp, start = 4.dp)
            .fillMaxHeight()
            .width(140.dp)
            .clickable { onTabClicked(tabState) },

        elevation = if (isActive) {
            64.dp
        } else {
            0.dp
        },
        color = if (isActive) {
            MaterialTheme.colors.surface
        } else {
            Color.Transparent
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        ) {
            Text(
                text = tabState.displayName.value,
                style = MaterialTheme.typography.caption,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(104.dp)
                    .wrapContentHeight()
                    .align(CenterVertically)
            )
            IconButton(
                onClick = { onCloseClicked(tabState) }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.content_description_close_tab)
                )
            }
        }
    }
}

@Composable
fun HomeIcon(isActive: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (isActive) {
            Icon(
                Icons.Default.Home,
                contentDescription = stringResource(id = R.string.content_description_home_button),
                tint = if (MaterialTheme.colors.isLight) {
                    Color.Companion.White
                } else {
                    MaterialTheme.colors.primaryVariant
                }
            )
        } else {
            Icon(
                Icons.Default.Home,
                contentDescription = stringResource(id = R.string.content_description_home_button)
            )
        }
    }
}

@Preview(name = "Tab Bottom Bar")
@Composable
fun TabBottomBarPreview() {
    VermilionTheme {
        TabBottomBarContent(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = 1,
            onHomeButtonClicked = {},
            onTabClicked = {},
            onTabCloseClicked = {}
        )
    }
}

@Preview(name = "Tab Bottom Bar (Dark)")
@Composable
fun TabBottomBarPreviewDark() {
    VermilionTheme(darkTheme = true) {
        TabBottomBarContent(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = 1,
            onHomeButtonClicked = {},
            onTabClicked = {},
            onTabCloseClicked = {}
        )
    }
}

@Preview(name = "Tab Bottom Bar Home Highlighted")
@Composable
fun TabBottomBarHomeHighlighted() {
    VermilionTheme {
        TabBottomBarContent(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = 0,
            onHomeButtonClicked = {},
            onTabClicked = {},
            onTabCloseClicked = {}
        )
    }
}

@Preview(name = "Tab Bottom Bar Home Highlighted (Dark)")
@Composable
fun TabBottomBarHomeHighlightedDark() {
    VermilionTheme(darkTheme = true) {
        TabBottomBarContent(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = 0,
            onHomeButtonClicked = {},
            onTabClicked = {},
            onTabCloseClicked = {}
        )
    }
}

private val DUMMY_TAB = TabState(
    TabId(0),
    ParentId("id"),
    TabType.POSTS,
    DisplayName("AskScience"),
    createdAt = Instant.now(),
    TabSortOrderIndex(1),
    com.neaniesoft.vermilion.coreentities.ScrollPosition(0, 0)
)

private val DUMMY_TAB_2 = DUMMY_TAB.copy(
    id = TabId(1),
    displayName = DisplayName("I danced a very long jig, AMA"),
    type = TabType.POST_DETAILS,
    tabSortOrder = TabSortOrderIndex(2)
)
