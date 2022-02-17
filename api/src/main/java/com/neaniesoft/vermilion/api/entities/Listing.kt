package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Listing(
    @JsonProperty("after") val after: String?,
    @JsonProperty("before") val before: String?,
    @JsonProperty("children") val children: List<Thing>
)

data class ListingResponse(
    @JsonProperty("data") val data: Listing
)
