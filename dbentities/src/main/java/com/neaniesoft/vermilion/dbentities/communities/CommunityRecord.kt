package com.neaniesoft.vermilion.dbentities.communities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "communities")
data class CommunityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val communityId: String,
    val name: String,
    val isSubscribed: Boolean
)
