package com.neaniesoft.vermilion.api.entities

import com.fasterxml.jackson.annotation.JsonProperty

sealed class ThingData

data class Link(
    @JsonProperty("id") val id: String,
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
    @JsonProperty("all_awardings") val allAwardings: List<Awarding>,
    @JsonProperty("post_hint") val postHint: String?,
    @JsonProperty("preview") val preview: Preview?
) : ThingData()

data class CommentData(
    @JsonProperty("id") val id: String,
    @JsonProperty("subreddit_id") val subredditId: String,
    @JsonProperty("subreddit") val subreddit: String,
    @JsonProperty("subreddit_name_prefixed") val subredditNamePrefixed: String,
    @JsonProperty("total_awards_received") val totalAwardsReceived: Int,
    @JsonProperty("replies") val replies: ListingResponse?,
    @JsonProperty("saved") val saved: Boolean,
    @JsonProperty("author") val author: String,
    @JsonProperty("created_utc") val createdUtc: Double,
    @JsonProperty("parent_id") val parentId: String,
    @JsonProperty("score") val score: Int,
    @JsonProperty("author_fullname") val authorFullname: String,
    @JsonProperty("body") val body: String,
    @JsonProperty("edited") val edited: Double,
    @JsonProperty("name") val name: String,
    @JsonProperty("is_submitter") val isSubmitter: Boolean,
    @JsonProperty("stickied") val sticked: Boolean,
    @JsonProperty("score_hidden") val scoreHidden: Boolean,
    @JsonProperty("permalink") val permalink: String,
    @JsonProperty("locked") val locked: Boolean,
    @JsonProperty("link_id") val linkId: String,
    @JsonProperty("controversiality") val controversiality: Int,
    @JsonProperty("depth") val depth: Int,
    @JsonProperty("ups") val ups: Int
) : ThingData()


