package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class RedditVideo(
    @JsonProperty("bitrate_kbps") val bitrateKbps: Int,
    @JsonProperty("fallback_url") val fallbackUrl: String,
    @JsonProperty("height") val height: Int,
    @JsonProperty("width") val width: Int,
    @JsonProperty("scrubber_media_url") val scrubberMediaUrl: String,
    @JsonProperty("dash_url") val dashUrl: String,
    @JsonProperty("duration") val duration: Int,
    @JsonProperty("hls_url") val hlsUrl: String,
    @JsonProperty("is_gif") val isGif: Boolean,
    @JsonProperty("transcoding_status") val transcodingStatus: String
)
