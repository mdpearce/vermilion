package com.neaniesoft.vermilion.postdetails.domain.entities

@JvmInline
value class CommentId(val value: String)

fun CommentId.fullName(): String = "t1_$value"
