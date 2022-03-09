package com.neaniesoft.vermilion.posts.domain.routing

sealed class CustomVideoMatchResult {
    object NoMatch : CustomVideoMatchResult()
    data class RouteMatch(val route: String) : CustomVideoMatchResult()
}
