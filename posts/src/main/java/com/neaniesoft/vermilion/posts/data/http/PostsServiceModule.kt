package com.neaniesoft.vermilion.posts.data.http

import com.neaniesoft.vermilion.api.RedditApiClientModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class PostsServiceModule {
    @Provides
    fun providePostsService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit): PostsService =
        retrofit.create()
}
