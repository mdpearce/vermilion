package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
sealed class Thing(open val data: ThingData)

@JsonTypeName("t3")
data class LinkThing(
    override val data: Link
) : Thing(data)

@JsonTypeName("t1")
data class CommentThing(
    override val data: CommentData
) : Thing(data)

@JsonTypeName("t5")
data class SubredditThing(
    override val data: SubredditData
) : Thing(data)

@JsonTypeName("more")
data class MoreCommentsThing(
    override val data: MoreCommentsData
) : Thing(data)
