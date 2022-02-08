package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

sealed class ThingData

data class Link(
    @JsonProperty("author") val author: String,
    @JsonProperty("domain") val domain: String,
    @JsonProperty("hidden") val hidden: Boolean,
    @JsonProperty("is_self") val isSelf: Boolean,
    @JsonProperty("created") val created: Double,
    @JsonProperty("num_comments") val numComments: Int,
    @JsonProperty("permalink") val permalink: String,
    @JsonProperty("score") val score: Int,
    @JsonProperty("selftext") val selfText: String,
    @JsonProperty("selftext_html") val selfTextHtml: String?,
    @JsonProperty("subreddit") val subreddit: String,
    @JsonProperty("thumbnail") val thumbnail: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("stickied") val stickied: Boolean
) : ThingData()