package com.neaniesoft.vermilion.communities.data.database

import com.neaniesoft.vermilion.api.entities.SubredditData
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.dbentities.communities.CommunityRecord

fun CommunityRecord.toCommunity(): Community {
    return NamedCommunity(
        CommunityName(name),
        CommunityId(communityId),
        isSubscribed = isSubscribed
    )
}

fun SubredditData.toCommunity(): Community {
    return NamedCommunity(
        CommunityName(displayName), CommunityId(id), isSubscribed = userIsSubscriber
    )
}

fun Community.toCommunityRecord(): CommunityRecord {
    if (this is NamedCommunity) {
        return CommunityRecord(
            id = 0,
            communityId = id.value,
            name = name.value,
            isSubscribed = isSubscribed
        )
    } else {
        throw IllegalArgumentException("Only named communities can be converted to records")
    }
}
