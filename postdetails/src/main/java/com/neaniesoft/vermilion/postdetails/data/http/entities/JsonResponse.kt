package com.neaniesoft.vermilion.postdetails.data.http.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonResponse(
    @JsonProperty("data") val data: JsonData
)
