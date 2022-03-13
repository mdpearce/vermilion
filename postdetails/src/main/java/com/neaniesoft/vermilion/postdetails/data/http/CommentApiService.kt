package com.neaniesoft.vermilion.postdetails.data.http

import com.neaniesoft.vermilion.api.RedditApiClientModule
import com.neaniesoft.vermilion.postdetails.data.http.entities.CommentResponse
import com.neaniesoft.vermilion.postdetails.data.http.entities.MoreCommentsResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Named

interface CommentApiService {
    @GET("/comments/{articleId}")
    suspend fun commentsForArticle(@Path("articleId") articleId: String): Array<CommentResponse>

    @GET("/comments/{articleId}")
    suspend fun commentsForArticleFocusedOn(
        @Query("comment") commentId: String,
        articleId: String
    ): Array<CommentResponse>

    @GET("/api/morechildren")
    suspend fun moreChildren(
        @Query("api_type") apiType: String,
        @Query("children", encoded = true) children: String,
        @Query("limit_children") limitChildren: Boolean,
        @Query("link_id") linkId: String,
        @Query("sort") sort: String
    ): MoreCommentsResponse
}

@Module
@InstallIn(SingletonComponent::class)
class CommentApiServiceModule {
    @Provides
    fun provideCommentApiService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit): CommentApiService =
        retrofit.create()
}
