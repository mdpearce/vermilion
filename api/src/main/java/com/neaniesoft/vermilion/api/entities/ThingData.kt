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
    @JsonProperty("preview") val preview: Preview?,
    @JsonProperty("secure_media") val secureMedia: SecureMedia?,
    @JsonProperty("link_flair_text") val linkFlairText: String?,
    @JsonProperty("link_flair_text_color") val linkFlairTextColor: String,
    @JsonProperty("link_flair_background_color") val linkFlairBackgroundColor: String?,
    @JsonProperty("link_flair_type") val linkFlairType: String,
    @JsonProperty("likes") val likes: Boolean?,
    @JsonProperty("is_gallery") val isGallery: Boolean?,
    @JsonProperty("media_metadata") val mediaMetadata: Map<String, MediaMetadataItem>?,
    @JsonProperty("gallery_data") val galleryData: GalleryData?
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
    @JsonProperty("author_fullname") val authorFullname: String?,
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
    @JsonProperty("ups") val ups: Int,
    @JsonProperty("collapsed") val collapsed: Boolean,
    @JsonProperty("distinguished") val distinguished: String?,
    @JsonProperty("author_flair_text") val authorFlairText: String?,
    @JsonProperty("author_flair_text_color") val authorFlairTextColor: String?,
    @JsonProperty("author_flair_background_color") val authorFlairBackgroundColor: String?,
    @JsonProperty("author_flair_type") val authorFlairType: String?,
    @JsonProperty("likes") val likes: Boolean?
) : ThingData()

data class MoreCommentsData(
    @JsonProperty("count") val count: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("id") val id: String,
    @JsonProperty("parent_id") val parentId: String,
    @JsonProperty("depth") val depth: Int,
    @JsonProperty("children") val children: List<String>
) : ThingData()

data class SubredditData(
    @JsonProperty("id") val id: String,
    @JsonProperty("display_name") val displayName: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("user_is_subscriber") val userIsSubscriber: Boolean
) : ThingData()

data class SecureMedia(
    @JsonProperty("reddit_video") val redditVideo: RedditVideo?
)

data class MediaMetadataItem(
    @JsonProperty("status") val status: String,
    @JsonProperty("m") val mediaType: String,
    @JsonProperty("p") val previews: List<MediaMetadataImage>,
    @JsonProperty("s") val source: MediaMetadataImage,
    @JsonProperty("id") val id: String
)

data class MediaMetadataImage(
    @JsonProperty("y") val height: Int,
    @JsonProperty("x") val width: Int,
    @JsonProperty("u") val uri: String
)

data class GalleryData(
    @JsonProperty("items") val items: List<GalleryItem>
)

data class GalleryItem(
    @JsonProperty("media_id") val mediaId: String,
    @JsonProperty("id") val id: Long
)
