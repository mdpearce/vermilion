package com.neaniesoft.vermilion.postdetails.ui

sealed class PostState {
    object Empty : PostState()
    object Error : PostState()
    data class Post(val post: com.neaniesoft.vermilion.posts.domain.entities.Post) : PostState()
}
