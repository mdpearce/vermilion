package com.neaniesoft.vermilion.postdetails.data.http.entities

import com.fasterxml.jackson.annotation.JsonProperty
import com.neaniesoft.vermilion.api.entities.Thing

data class JsonData(
    @JsonProperty("things") val things: List<Thing>
)
