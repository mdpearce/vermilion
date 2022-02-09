package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
sealed class Thing(open val data: ThingData)

@JsonTypeName("t3")
data class LinkThing(
    override val data: Link
) : Thing(data)
