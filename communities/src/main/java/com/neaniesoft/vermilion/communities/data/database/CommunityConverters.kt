package com.neaniesoft.vermilion.communities.data.database

import com.neaniesoft.vermilion.api.entities.SubredditData
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import java.time.Clock

fun SubredditData.toCommunity(): Community {
    return NamedCommunity(
        CommunityName(displayName),
        CommunityId(id),
        isSubscribed = userIsSubscriber
    )
}

fun Community.toCommunitySqlRecord(clock: Clock): com.neaniesoft.vermilion.db.Community {
    if (this is NamedCommunity) {
        return com.neaniesoft.vermilion.db.Community(
            id = 0,
            inserted_at = clock.millis(),
            community_id = id.value,
            name = name.value,
            is_subscribed = if (isSubscribed) { 1L } else { 0L }
        )
    } else {
        throw IllegalArgumentException("Only named communities can be converted to records")
    }
}
