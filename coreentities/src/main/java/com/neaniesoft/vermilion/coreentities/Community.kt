package com.neaniesoft.vermilion.coreentities

sealed class Community(val routeName: String)

data class NamedCommunity(
    val name: CommunityName,
    val id: CommunityId,
    val isSubscribed: Boolean = false
) : Community(name.value)

object FrontPage : Community("FrontPage")
