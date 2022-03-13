package com.neaniesoft.vermilion.postdetails.domain.entities

import com.neaniesoft.vermilion.posts.domain.entities.PostId

data class ThreadStub(
    val postId: PostId,
    val parentId: CommentId,
    val depth: CommentDepth
)
