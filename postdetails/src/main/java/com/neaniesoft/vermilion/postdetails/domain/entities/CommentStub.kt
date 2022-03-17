package com.neaniesoft.vermilion.postdetails.domain.entities

import com.neaniesoft.vermilion.posts.domain.entities.PostId

data class CommentStub(
    val postId: PostId,
    val id: CommentId,
    override val path: String,
    val count: MoreCommentsCount,
    val parentId: CommentId?,
    val depth: CommentDepth,
    val children: List<CommentId>,
    override val isHidden: Boolean = false
) : Hideable, HasPath
