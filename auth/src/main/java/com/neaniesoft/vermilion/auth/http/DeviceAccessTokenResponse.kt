package com.neaniesoft.vermilion.auth.http

import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceAccessTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("device_id") val deviceId: String,
    @JsonProperty("expires_in") val expiresInSeconds: Long,
    @JsonProperty("scope") val scope: String
)
