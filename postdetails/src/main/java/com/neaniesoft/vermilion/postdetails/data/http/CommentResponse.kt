package com.neaniesoft.vermilion.postdetails.data.http

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.entities.Listing

data class CommentResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: Listing
)
