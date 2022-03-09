package com.neaniesoft.vermilion.ui.videos.external.resolver

sealed class VideoResolverError
object NoRegisteredResolvers : VideoResolverError()
object InvalidUriForResolver : VideoResolverError()
data class ApiError(val cause: Throwable) : VideoResolverError()
