package com.neaniesoft.vermilion.ui.videos.external.redgifs

import com.fasterxml.jackson.annotation.JsonProperty

data class GifInfo(
    @JsonProperty("id") val id: String,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("urls") val urls: MediaInfo
)
