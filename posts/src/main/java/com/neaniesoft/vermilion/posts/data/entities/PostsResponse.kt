package com.neaniesoft.vermilion.posts.data.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.entities.Listing

data class PostsResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: Listing
)
