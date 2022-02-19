package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import java.time.Instant

@Composable
fun TabBottomBar(
    tabs: List<TabState>,
    activeTab: ActiveTab,
    onHomeButtonClicked: () -> Unit,
    onUserButtonClicked: () -> Unit,
    onTabClicked: (TabState) -> Unit,
    onTabCloseClicked: (TabState) -> Unit
) {
    BottomAppBar() {
        LazyRow(
            Modifier
                .fillMaxWidth()
        ) {
            item {
                IconButton(onClick = onHomeButtonClicked) {
                    Icon(Icons.Default.Home, contentDescription = null)
                }
            }
            item {
                IconButton(onClick = onUserButtonClicked) {
                    Icon(Icons.Default.Person, contentDescription = null)
                }
            }

            items(tabs) { tab ->

                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .padding(end = 4.dp, start = 4.dp)
                        .wrapContentHeight()
                        .width(140.dp)
                        .clickable { onTabClicked(tab) }
                        .alpha(
                            if (activeTab is ActiveTab.Tab && tab.id == activeTab.id) {
                                1f
                            } else {
                                0.5f
                            }
                        ),
                    color = MaterialTheme.colors.surface,
                    elevation = if (activeTab is ActiveTab.Tab && tab.id == activeTab.id) {
                        48.dp
                    } else {
                        16.dp
                    }
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp)
                    ) {
                        Text(
                            text = tab.displayName.value,
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .width(104.dp)
                                .wrapContentHeight()
                                .align(CenterVertically)
                        )
                        IconButton(
                            onClick = { onTabCloseClicked(tab) }) {
                            Icon(Icons.Default.Close, contentDescription = "close")
                        }
                    }
                }

            }
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
            onUserButtonClicked = {},
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
            onUserButtonClicked = {},
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
    ScrollPosition(0)
)

private val DUMMY_TAB_2 = DUMMY_TAB.copy(
    id = TabId(1),
    displayName = DisplayName("I danced a very long jig, AMA"),
    type = TabType.POST_DETAILS,
    tabSortOrder = TabSortOrderIndex(2)
)

sealed class ActiveTab {
    object None : ActiveTab()
    data class Tab(val id: TabId) : ActiveTab()
}

