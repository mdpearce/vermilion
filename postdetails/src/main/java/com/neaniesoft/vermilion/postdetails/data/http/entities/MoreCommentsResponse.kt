package com.neaniesoft.vermilion.postdetails.data.http.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class MoreCommentsResponse(
    @JsonProperty("json") val json: JsonResponse
)
