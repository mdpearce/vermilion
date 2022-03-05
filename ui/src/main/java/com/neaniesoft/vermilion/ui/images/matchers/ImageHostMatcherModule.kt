package com.neaniesoft.vermilion.ui.images.matchers

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageHostMatcherModule {

    @Binds
    @IntoSet
    abstract fun provideRedditMatcher(redditImageHostMatcher: RedditImageHostMatcher): ImageHostMatcher

    @Binds
    @IntoSet
    abstract fun provideImgurMatcher(imgurImagerHostMatcher: ImgurImagerHostMatcher): ImageHostMatcher
}
