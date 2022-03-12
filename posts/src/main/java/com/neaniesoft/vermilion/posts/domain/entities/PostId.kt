package com.neaniesoft.vermilion.posts.domain.entities

@JvmInline
value class PostId(val value: String)

fun PostId.fullName(): String = "t3_$value"
