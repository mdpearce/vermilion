package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL

data class Award(
    val name: AwardName,
    val iconUrl: URL
)
