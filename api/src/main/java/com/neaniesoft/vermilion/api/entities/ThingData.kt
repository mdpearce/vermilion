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
    @JsonProperty("subreddit_name_prefixed") val subredditNamePrefixed: String,
    @JsonProperty("subreddit_id") val subredditId: String,
    @JsonProperty("thumbnail") val thumbnail: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("stickied") val stickied: Boolean,
    @JsonProperty("saved") val saved: Boolean,
    @JsonProperty("over_18") val over18: Boolean?,
    @JsonProperty("all_awardings") val allAwardings: List<Awarding>
) : ThingData()

data class Awarding(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("icon_url") val iconUrl: String,
    @JsonProperty("resized_icons") val resizedIcons: List<UrlImage>,
    @JsonProperty("icon_width") val iconWidth: Int,
    @JsonProperty("icon_height") val iconHeight: Int,
    @JsonProperty("description") val description: String,
    @JsonProperty("count") val count: Int
)

data class UrlImage(
    @JsonProperty("url") val url: String,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int
)