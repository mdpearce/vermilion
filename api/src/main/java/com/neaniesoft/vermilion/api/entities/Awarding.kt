package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Awarding(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("icon_url") val iconUrl: String,
    @JsonProperty("resized_icons") val resizedIcons: List<UrlImage>,
    @JsonProperty("icon_width") val iconWidth: Int,
    @JsonProperty("icon_height") val iconHeight: Int,
    @JsonProperty("description") val description: String,
    @JsonProperty("count") val count: Int
)
