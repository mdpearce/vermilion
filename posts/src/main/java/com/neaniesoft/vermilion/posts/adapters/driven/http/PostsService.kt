package com.neaniesoft.vermilion.posts.adapters.driven.http

import com.neaniesoft.vermilion.posts.adapters.driven.entities.PostsResponse
import retrofit2.http.GET

interface PostsService {
    @GET("best")
    suspend fun frontPageBest(): PostsResponse
}
