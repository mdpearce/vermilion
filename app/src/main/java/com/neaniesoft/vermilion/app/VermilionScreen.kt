package com.neaniesoft.vermilion.app

enum class VermilionScreen {
    Posts;

    companion object {
        fun fromRoute(route: String?): VermilionScreen = when (route?.substringBefore("/")) {
            Posts.name -> Posts
            null -> Posts
            else -> throw IllegalArgumentException("Unrecognized route: $route")
        }
    }
}
