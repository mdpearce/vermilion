package com.neaniesoft.vermilion.postdetails.data.http

import com.neaniesoft.vermilion.api.RedditApiClientModule
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

@Module
@InstallIn(SingletonComponent::class)
class CommentApiServiceModule {
    @Provides
    fun provideCommentApiService(@Named(RedditApiClientModule.AUTHENTICATED) retrofit: Retrofit): CommentApiService =
        retrofit.create()
}
