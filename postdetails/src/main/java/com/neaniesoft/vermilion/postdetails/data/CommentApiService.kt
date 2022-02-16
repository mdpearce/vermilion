package com.neaniesoft.vermilion.postdetails.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.RedditApiClientModule
import com.neaniesoft.vermilion.api.entities.Listing
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Named

interface CommentApiService {
    @GET("/comments/{articleId}")
    suspend fun commentsForArticle(@Path("articleId") articleId: String): Array<CommentResponse>
}

data class CommentResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: Listing
)

@Module
@InstallIn(SingletonComponent::class)
class CommentApiServiceModule {
    @Provides
    fun provideCommentApiService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit): CommentApiService =
        retrofit.create()
}
