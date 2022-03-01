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
