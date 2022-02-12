package com.neaniesoft.vermilion.posts.adapters.driven.http

import com.neaniesoft.vermilion.posts.adapters.driven.entities.PostsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PostsService {
    @GET("best")
    suspend fun frontPageBest(
        @Query("before") before: String?,
        @Query("after") after: String?,
        @Query("count") count: Int?
    ): PostsResponse
}
