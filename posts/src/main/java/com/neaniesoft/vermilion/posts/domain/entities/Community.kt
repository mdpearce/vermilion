package com.neaniesoft.vermilion.posts.domain.entities

sealed class Community

data class NamedCommunity(
    val communityName: CommunityName
) : Community()

object FrontPage : Community()