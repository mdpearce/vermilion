package com.neaniesoft.vermilion.dbentities.posts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_history")
data class PostHistoryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val postId: String,
    val visitedAt: Long
)
