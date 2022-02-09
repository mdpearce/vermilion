package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class UrlImage(
    @JsonProperty("url") val url: String,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int
)