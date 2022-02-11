package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Images(
    @JsonProperty("source") val source: UrlImage,
    @JsonProperty("resolutions") val resolutions: List<UrlImage>
)
