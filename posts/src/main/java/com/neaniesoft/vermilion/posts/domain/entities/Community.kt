package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL

sealed class Community

data class NamedCommunity(
    val name: CommunityName,
    val iconUrl: URL? = null
) : Community()

object FrontPage : Community()
