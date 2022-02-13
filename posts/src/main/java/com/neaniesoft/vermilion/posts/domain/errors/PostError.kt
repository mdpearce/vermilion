package com.neaniesoft.vermilion.posts.domain.errors

sealed class PostError(override val cause: Throwable? = null) : Error()

class PostsApiError(cause: Throwable) : PostError(cause)
class PostsPersistenceError(cause: Throwable) : PostError(cause)
