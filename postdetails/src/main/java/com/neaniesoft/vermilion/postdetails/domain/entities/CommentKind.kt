package com.neaniesoft.vermilion.postdetails.domain.entities

sealed class CommentKind(val depth: CommentDepth) {
    data class Full(val comment: Comment) : CommentKind(comment.depth)
    data class Stub(val stub: CommentStub) : CommentKind(stub.depth)
    data class Thread(val stub: ThreadStub) : CommentKind(stub.depth)
}
