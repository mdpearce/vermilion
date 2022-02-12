package com.neaniesoft.vermilion.posts.data.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.entities.Thing

data class ListingResponse(
    @JsonProperty("after") val after: String?,
    @JsonProperty("before") val before: String?,
    @JsonProperty("children") val children: List<Thing>
)
