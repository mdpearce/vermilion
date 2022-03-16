package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import android.util.Log
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.tabs.R
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.uistate.TabType
import java.time.Instant

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
            Log.d("TabBottomBarContent", "Drawing tab index $index ${tabState.displayName}")
            ContentTab(
                tabState = tabState,
                isSelected = activeTab == index + 1,
                onClick = {
                    Log.d("TabBottomBar", "Clicked index $index - ${tabs[index]}")
                    onTabClicked(tabs[index])
                },
                onLongPress = onTabCloseClicked
            )
        }
    }
}

@Composable
fun ContentTab(
    tabState: TabState,
    isSelected: Boolean,
    onClick: (TabState) -> Unit,
    onLongPress: (TabState) -> Unit
) {
    Tab(selected = isSelected,
        onClick = {},
        content = {
            val interactionSource = remember(tabState) { MutableInteractionSource() }

            Row(
                Modifier
                    .indication(interactionSource, LocalIndication.current)
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
                    .fillMaxWidth()
                    .pointerInput(tabState) {
                        detectTapGestures(
                            onPress = { offset ->
                                val press = PressInteraction.Press(offset)
                                interactionSource.emit(press)

                                tryAwaitRelease()

                                interactionSource.emit(PressInteraction.Release(press))
                            },

                            onTap = {
                                onClick(tabState)
                            },

                            onLongPress = {
                                onLongPress(tabState)
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
