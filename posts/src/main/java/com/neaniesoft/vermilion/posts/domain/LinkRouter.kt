package com.neaniesoft.vermilion.posts.domain

import android.net.Uri
import com.neaniesoft.vermilion.posts.domain.routing.CustomVideoRouter
import com.neaniesoft.vermilion.ui.images.ImageRouter
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoUriResolverSupervisor
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

interface LinkRouter {
    fun routeForLink(uri: Uri): String
}

@Singleton
class LinkRouterImpl @Inject constructor(
    private val videoUriResolver: VideoUriResolverSupervisor,
    private val customVideoRouter: CustomVideoRouter,
    private val imageRouter: ImageRouter
) : LinkRouter {
    private val logger by logger()

    override fun routeForLink(uri: Uri): String {
        logger.debugIfEnabled { "Building route for link: $uri" }
        if (videoUriResolver.canAnyResolverHandle(uri)) {
            logger.debugIfEnabled { "Link $uri is an external video" }
            return buildExternalVideoRoute(uri)
        }
        val customVideoRoute = customVideoRouter.routeForVideoUri(uri)
        if (customVideoRoute != null) {
            logger.debugIfEnabled { "Link $uri is a custom video" }
            return customVideoRoute
        }
        val directImageUri = imageRouter.directImageUriOrNull(uri)
        if (directImageUri != null) {
            logger.debugIfEnabled { "Link $uri is a direct image" }
            return buildImageRoute(directImageUri)
        }

        // If all else fails, just open a custom tab
        logger.debugIfEnabled { "Link $uri did not match any custom routes. Falling back to custom tab" }
        return customTabRoute(uri)
    }

    private fun buildImageRoute(uri: Uri): String {
        return "Image/" + Uri.encode(uri.toString())
    }

    private fun buildExternalVideoRoute(uri: Uri): String {
        return "ExternalVideo/" + Uri.encode(uri.toString())
    }

    private fun customTabRoute(uri: Uri): String =
        "CustomTab/" + URLEncoder.encode(uri.toString(), "utf-8")
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LinkRouterModule {
    @Binds
    abstract fun bindLinkRouterImpl(impl: LinkRouterImpl): LinkRouter
}
