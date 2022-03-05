package com.neaniesoft.vermilion.postdetails.domain.entities


sealed class CommentFlair {
    object NoFlair : CommentFlair()
    data class TextFlair(
        val text: CommentFlairText,
        val backgroundColor: CommentFlairBackgroundColor,
        val textColor: CommentFlairTextColor
    ) : CommentFlair()
}
