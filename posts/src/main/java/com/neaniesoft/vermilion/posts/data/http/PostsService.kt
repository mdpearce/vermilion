package com.neaniesoft.vermilion.posts.data.http

import com.neaniesoft.vermilion.posts.data.entities.PostsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostsService {
    @GET("best")
    suspend fun frontPageBest(
        @Query("limit") limit: Int,
        @Query("before") before: String?,
        @Query("after") after: String?,
        @Query("count") count: Int?
    ): PostsResponse

    @GET("r/{communityName}/best")
    suspend fun subredditBest(
        @Path("communityName") communityName: String,
        @Query("limit") limit: Int,
        @Query("before") before: String?,
        @Query("after") after: String?,
        @Query("count") count: Int?
    ): PostsResponse

    @FormUrlEncoded
    @POST("api/vote")
    suspend fun vote(
        @Field("dir") direction: Int,
        @Field("id") fullNameId: String
    )
}
