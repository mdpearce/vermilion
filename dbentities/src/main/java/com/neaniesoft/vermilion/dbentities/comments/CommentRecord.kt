package com.neaniesoft.vermilion.dbentities.comments

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val commentId: String,
    val postId: String,
    val parentId: String?,
    val path: String?,
    val body: String,
    val flags: String,
    val author: String,
    val createdAt: Long,
    val insertedAt: Long,
    val score: Int,
    val link: String,
    val controversialIndex: Int,
    val depth: Int,
    val upVotes: Int,
    val flairText: String?,
    val flairBackgroundColor: Int,
    val flairTextColor: String
)
