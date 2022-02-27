package com.neaniesoft.vermilion.communities.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.communities.R
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@ExperimentalFoundationApi
@Composable
fun CommunityList(
    communities: List<Community>,
    onCommunityClicked: (Community) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxWidth()) {
        stickyHeader {
            Surface(elevation = 8.dp) {
                Column {
                    Text(
                        text = stringResource(id = R.string.community_list_title),
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.subtitle2
                    )
                    Divider()
                }
            }
        }

        items(communities) { community ->
            Box(
                Modifier
                    .clickable { onCommunityClicked(community) }
                    .fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = (community as NamedCommunity).name.value
                )
            }
        }

    }
}

val PREVIEW_COMMUNITIES: List<Community> = (1..10).map { i ->
    NamedCommunity(CommunityName("Community $i"), CommunityId("community$i"), isSubscribed = true)
}

@ExperimentalFoundationApi
@Preview(name = "Community List Light")
@Composable
fun CommunityListLightPreview() {
    VermilionTheme {
        CommunityList(
            communities = PREVIEW_COMMUNITIES,
            onCommunityClicked = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@ExperimentalFoundationApi
@Preview(name = "Community List Dark")
@Composable
fun CommunityListDarkPreview() {
    VermilionTheme(darkTheme = true) {
        CommunityList(
            communities = PREVIEW_COMMUNITIES,
            onCommunityClicked = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
