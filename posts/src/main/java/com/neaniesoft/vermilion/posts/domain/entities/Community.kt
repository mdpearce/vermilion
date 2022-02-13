package com.neaniesoft.vermilion.posts.domain.entities

sealed class Community

data class NamedCommunity(
    val name: CommunityName
) : Community()

object FrontPage : Community()
