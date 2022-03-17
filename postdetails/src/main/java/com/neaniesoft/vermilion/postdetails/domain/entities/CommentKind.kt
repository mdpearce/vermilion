package com.neaniesoft.vermilion.postdetails.domain.entities

sealed class CommentKind {
    data class Full(val comment: Comment) : CommentKind()
    data class Stub(val stub: CommentStub) : CommentKind()
    data class Thread(val stub: ThreadStub) : CommentKind()
}

interface Hideable {
    val isHidden: Boolean
}

interface HasPath {
    val path: String
}
