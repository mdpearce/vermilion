package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Preview(
    @JsonProperty("images") val images: List<Images>,
    @JsonProperty("enabled") val enabled: Boolean
)
