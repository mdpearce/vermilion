package com.neaniesoft.vermilion.posts.domain.routing

import android.net.Uri

interface CustomVideoRouteMatcher {
    fun match(linkUri: Uri): CustomVideoMatchResult
}
