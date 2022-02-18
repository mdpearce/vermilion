package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.tabs.R
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.Route
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
    onUserButtonClicked: () -> Unit,
    onTabClicked: (TabState) -> Unit
) {
    BottomAppBar(Modifier.height(IntrinsicSize.Min)) {
        LazyRow(
            Modifier
                .fillMaxWidth()
        ) {
            item {
                IconButton(onClick = onUserButtonClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_account_circle_24),
                        contentDescription = null
                    )
                }
            }

            items(tabs) { tab ->

                Surface(
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight()
                        .width(128.dp),
                    color = MaterialTheme.colors.primaryVariant,
                    elevation = 24.dp
                ) {
                    Text(
                        text = tab.displayName.value,
                        style = MaterialTheme.typography.button,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(6.dp)
                            .wrapContentHeight()
                    )
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
            onUserButtonClicked = {},
            onTabClicked = {}
        )
    }
}

@Preview(name = "Tab Bottom Bar (Dark)")
@Composable
fun TabBottomBarPreviewDark() {
    VermilionTheme(darkTheme = true) {
        TabBottomBar(
            tabs = listOf(DUMMY_TAB, DUMMY_TAB_2),
            onUserButtonClicked = {},
            onTabClicked = {}
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
    displayName = DisplayName("I danced a jig, AMA"),
    type = TabType.POST_DETAILS,
    tabSortOrder = TabSortOrderIndex(2)
)
