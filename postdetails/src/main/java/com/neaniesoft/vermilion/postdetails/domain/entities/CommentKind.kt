package com.neaniesoft.vermilion.postdetails.domain.entities

sealed class CommentKind(val depth: CommentDepth, val isHidden: Boolean) {
    data class Full(val comment: Comment) : CommentKind(comment.depth, comment.isHidden)
    data class Stub(val stub: CommentStub) : CommentKind(stub.depth, stub.isHidden)
    data class Thread(val stub: ThreadStub) : CommentKind(stub.depth, stub.isHidden)
}
