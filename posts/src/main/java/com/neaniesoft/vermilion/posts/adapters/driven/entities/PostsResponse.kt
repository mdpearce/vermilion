package com.neaniesoft.vermilion.posts.adapters.driven.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class PostsResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: ListingResponse
)