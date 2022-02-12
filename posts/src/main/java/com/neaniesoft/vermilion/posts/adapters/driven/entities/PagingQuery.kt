package com.neaniesoft.vermilion.posts.adapters.driven.entities

import com.neaniesoft.vermilion.posts.domain.entities.ListingKey

data class PagingQuery(
    val listingKey: ListingKey,
    val previousCount: Int
)
