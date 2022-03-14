package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.neaniesoft.vermilion.tabs.R
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ActiveTab
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject

@Composable
fun TabBottomBar(
    tabs: List<TabState>,
    activeTab: ActiveTab,
    onHomeButtonClicked: () -> Unit,
    onTabClicked: (TabState) -> Unit,
    onTabCloseClicked: (TabState) -> Unit
) {
    BottomAppBar(elevation = 16.dp) {
        LazyRow(
            Modifier
                .fillMaxWidth()
        ) {
            item {
                HomeIcon(isActive = activeTab is ActiveTab.Home, onClick = onHomeButtonClicked)
            }

            items(tabs) { tab ->
                if (tab.type != TabType.HOME) {
                    val isActive = activeTab is ActiveTab.Tab && tab.id == activeTab.id

                    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
                        TopLevelTab(
                            isActive = isActive,
                            tabState = tab,
                            onTabClicked = onTabClicked,
                            onCloseClicked = onTabCloseClicked
                        )
                    }
                }
            }
        }
    }
}

@HiltViewModel
class TabBottomBarViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor
) : ViewModel() {
    val tabs = tabSupervisor.currentTabs
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
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = ActiveTab.Tab(DUMMY_TAB.id),
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
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = ActiveTab.Tab(DUMMY_TAB.id),
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
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = ActiveTab.Home,
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
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = ActiveTab.Home,
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
    ScrollPosition(0, 0)
)

private val DUMMY_TAB_2 = DUMMY_TAB.copy(
    id = TabId(1),
    displayName = DisplayName("I danced a very long jig, AMA"),
    type = TabType.POST_DETAILS,
    tabSortOrder = TabSortOrderIndex(2)
)
