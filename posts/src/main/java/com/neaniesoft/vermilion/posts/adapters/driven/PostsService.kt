package com.neaniesoft.vermilion.posts.adapters.driven

import com.fasterxml.jackson.annotation.JsonProperty
import com.vermilion.api.ListingResponse
import com.vermilion.api.RedditApiClientModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import javax.inject.Named

interface PostsService {
    @GET("best")
    suspend fun frontPageBest(): PostsResponse
}

data class PostsResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: ListingResponse
)

@Module
@InstallIn(SingletonComponent::class)
class PostsServiceModule {
    @Provides
    fun providePostsService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit): PostsService =
        retrofit.create()
}