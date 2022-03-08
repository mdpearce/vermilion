package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

data class Preview(
    @JsonProperty("images") val images: List<Images>,
    @JsonProperty("reddit_video_preview") val redditVideoPreview: RedditVideo?,
    @JsonProperty("enabled") val enabled: Boolean
)
