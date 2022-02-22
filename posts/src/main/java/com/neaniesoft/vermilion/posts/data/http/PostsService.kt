package com.neaniesoft.vermilion.posts.data.http

import com.neaniesoft.vermilion.posts.data.entities.PostsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PostsService {
    @GET("r/castrecordings/best")
    suspend fun frontPageBest(
        @Query("limit") limit: Int,
        @Query("before") before: String?,
        @Query("after") after: String?,
        @Query("count") count: Int?
    ): PostsResponse
}
