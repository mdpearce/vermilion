package com.neaniesoft.vermilion.postdetails.data.http

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.entities.Listing
import com.neaniesoft.vermilion.api.entities.Thing

data class CommentResponse(
    @JsonProperty("kind") val kind: String,
    @JsonProperty("data") val data: Listing
)

data class MoreCommentsResponse(
    @JsonProperty("json") val json: JsonResponse
)

data class JsonResponse(
    @JsonProperty("data") val data: JsonData
)

data class JsonData(
    @JsonProperty("things") val things: List<Thing>
)
