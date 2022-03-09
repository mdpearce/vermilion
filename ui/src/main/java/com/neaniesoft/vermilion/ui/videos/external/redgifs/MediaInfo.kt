package com.neaniesoft.vermilion.ui.videos.external.redgifs

import com.fasterxml.jackson.annotation.JsonProperty

data class MediaInfo(
    @JsonProperty("sd") val sd: String,
    @JsonProperty("hd") val hd: String
)
