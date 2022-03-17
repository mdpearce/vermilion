package com.neaniesoft.vermilion.postdetails.domain.entities

import com.neaniesoft.vermilion.posts.domain.entities.PostId

data class ThreadStub(
    val postId: PostId,
    override val path: String,
    val parentId: CommentId,
    val depth: CommentDepth,
    override val isHidden: Boolean = false
) : Hideable, HasPath
