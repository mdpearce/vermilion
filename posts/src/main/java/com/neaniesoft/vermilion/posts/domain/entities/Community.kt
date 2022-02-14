package com.neaniesoft.vermilion.posts.domain.entities

sealed class Community(val routeName: String)

data class NamedCommunity(
    val name: CommunityName
) : Community(name.value)

object FrontPage : Community("FrontPage")
