package com.neaniesoft.vermilion.ui.videos.external.redgifs

import retrofit2.http.GET
import retrofit2.http.Path

interface RedGifsApi {
    @GET("/v2/gifs/{id}")
    suspend fun getGif(@Path("id") id: String): GifResponse
}
