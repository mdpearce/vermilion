package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRouter @Inject constructor(private val matchers: Set<@JvmSuppressWildcards ImageHostMatcher>) {
    private val logger by logger()

    fun directImageUriOrNull(uri: Uri): Uri? {
        matchers.forEach { matcher ->
            val result = matcher.match(uri)
            if (result is ImageHostMatchResult.DirectImageUri) {
                logger.debugIfEnabled { "Matched a direct image URI: ${result.uri}" }
                return result.uri
            }
        }
        return null
    }
}

interface ImageHostMatcher {
    fun match(uri: Uri): ImageHostMatchResult
}

sealed class ImageHostMatchResult {
    object NoMatch : ImageHostMatchResult()
    data class DirectImageUri(val uri: Uri) : ImageHostMatchResult()
}

@Singleton
class RedditImageHostMatcher @Inject constructor() : ImageHostMatcher {
    override fun match(uri: Uri): ImageHostMatchResult {
        return if (uri.host == "i.redd.it") {
            ImageHostMatchResult.DirectImageUri(uri)
        } else {
            ImageHostMatchResult.NoMatch
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageHostMatcherModule {

    @Binds
    @IntoSet
    abstract fun provideImageHostMatchers(redditImageHostMatcher: RedditImageHostMatcher): ImageHostMatcher
}
