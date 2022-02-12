package com.neaniesoft.vermilion.posts.domain.entities

data class ResultSet<out T>(
    val results: List<T>,
    val beforeKey: BeforeKey?,
    val afterKey: AfterKey?,
    val previousResultCount: Int
)

sealed class ListingKey

data class BeforeKey(val value: String) : ListingKey()

data class AfterKey(val value: String) : ListingKey()

object FirstSet : ListingKey()
