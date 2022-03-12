package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Image(
    @JsonProperty("source") val source: UrlImage,
    @JsonProperty("resolutions") val resolutions: List<UrlImage>,
    @JsonProperty("variants") val variants: Map<String, Image>?
)
