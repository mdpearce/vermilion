package com.neaniesoft.vermilion.posts.domain.routing

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class CustomVideoRouterModule {
    @Binds
    @IntoSet
    abstract fun bindYoutubeCustomVideoRouteMatcher(impl: YoutubeCustomVideoRouteMatcher): CustomVideoRouteMatcher
}
