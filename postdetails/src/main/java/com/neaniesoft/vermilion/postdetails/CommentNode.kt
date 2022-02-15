package com.neaniesoft.vermilion.postdetails

data class CommentNode(
    val id: CommentId,
    val content: CommentContent,
    val children: List<CommentNode>
)

@JvmInline
value class CommentId(val value: String)

@JvmInline
value class CommentContent(val value: String)
