package com.neaniesoft.vermilion.ui.videos.external.redgifs

import com.fasterxml.jackson.annotation.JsonProperty

data class GifResponse(
    @JsonProperty("gif") val gif: GifInfo
)
