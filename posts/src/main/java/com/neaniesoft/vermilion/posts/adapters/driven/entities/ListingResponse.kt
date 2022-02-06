package com.neaniesoft.vermilion.posts.adapters.driven.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.vermilion.api.Thing

data class ListingResponse(
    @JsonProperty("after") val after: String?,
    @JsonProperty("before") val before: String?,
    @JsonProperty("children") val children: List<Thing>
)