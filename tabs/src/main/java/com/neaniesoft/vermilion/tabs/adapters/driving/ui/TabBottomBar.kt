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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.tabs.R
import com.neaniesoft.vermilion.tabs.domain.entities.ActiveTab
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
    BottomAppBar(elevation = 16.dp) {
        LazyRow(
            Modifier
                .fillMaxWidth()
        ) {
            item {
                HomeIcon(isActive = activeTab is ActiveTab.Home, onClick = onHomeButtonClicked)
            }

            item {
                IconButton(onClick = onUserButtonClicked) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = stringResource(id = R.string.content_description_my_account_button)
                    )
                }
            }

            items(tabs) { tab ->
                val isActive = activeTab is ActiveTab.Tab && tab.id == activeTab.id

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

@Composable
fun TopLevelTab(
    isActive: Boolean,
    tabState: TabState,
    onTabClicked: (TabState) -> Unit,
    onCloseClicked: (TabState) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .padding(end = 4.dp, start = 4.dp)
            .wrapContentHeight()
            .width(140.dp)
            .clickable { onTabClicked(tabState) }
            .alpha(
                if (isActive) {
                    1f
                } else {
                    0.5f
                }
            ),
        color = MaterialTheme.colors.surface,
        elevation = if (isActive) {
            64.dp
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

@Preview(name = "Tab Bottom Bar Home Highlighted")
@Composable
fun TabBottomBarHomeHighlighted() {
    VermilionTheme {
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            activeTab = ActiveTab.Home,
            onHomeButtonClicked = {},
            onUserButtonClicked = {},
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
            onUserButtonClicked = {},
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
