package com.neaniesoft.vermilion.posts.domain.entities

data class ResultSet<out T>(
    val results: List<T>,
)
