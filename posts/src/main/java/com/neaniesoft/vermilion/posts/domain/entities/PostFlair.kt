package com.neaniesoft.vermilion.posts.domain.entities

sealed class PostFlair {
    object NoFlair : PostFlair()
    data class TextFlair(
        val text: PostFlairText,
        val backgroundColor: PostFlairBackgroundColor,
        val textColor: PostFlairTextColor
    ) : PostFlair()
}
