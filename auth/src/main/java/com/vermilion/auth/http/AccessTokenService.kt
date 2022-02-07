package com.vermilion.auth.http

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AccessTokenService {
    @FormUrlEncoded
    @POST("access_token")
    fun deviceAccessToken(
        @Field("grant_type") grantType: String,
        @Field("device_id") deviceId: String
    ): Call<DeviceAccessTokenResponse>
}