package com.neaniesoft.vermilion.coreentities

sealed class Community(val routeName: String)

data class NamedCommunity(
    val name: CommunityName,
    val id: CommunityId
) : Community(name.value)

object FrontPage : Community("FrontPage")
