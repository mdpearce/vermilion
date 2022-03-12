package com.neaniesoft.vermilion.dbentities.posts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "posts")
data class PostRecord(
    @PrimaryKey val id: String,
    val postId: String,
    val query: String,
    val insertedAt: Long,
    val title: String,
    val postType: PostType,
    val linkHost: String,
    val thumbnailUri: String?,
    val previewUri: String?,
    val previewWidth: Int?,
    val previewHeight: Int?,
    val previewVideoWidth: Int?,
    val previewVideoHeight: Int?,
    val previewVideoDash: String?,
    val previewVideoHls: String?,
    val previewVideoFallback: String?,
    val animatedPreviewWidth: Int?,
    val animatedPreviewHeight: Int?,
    val animatedPreviewUri: String?,
    val videoWidth: Int?,
    val videoHeight: Int?,
    val videoDash: String?,
    val videoHls: String?,
    val videoFallback: String?,
    val linkUri: String,
    val previewText: String?,
    val communityName: String,
    val communityId: String,
    val authorName: String,
    val postedAt: Long,
    // val awards TODO implement caching of awards
    val commentCount: Int,
    val score: Int,
    val flags: String,
    val flairText: String?,
    val flairBackgroundColor: Int,
    val flairTextColor: String
)

enum class PostType {
    IMAGE,
    LINK,
    TEXT,
    VIDEO
}

data class PostWithHistory(
    @Embedded val post: PostRecord,
    @Relation(
        parentColumn = "postId",
        entityColumn = "postId"
    )
    val history: List<PostHistoryRecord>
)
