package com.neaniesoft.vermilion.dbentities.posts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_remote_keys")
data class PostRemoteKey(
    @PrimaryKey val label: String,
    val nextKey: String?
)
