package com.neaniesoft.vermilion.dbentities.tabs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabStateRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val type: String,
    val parentId: String,
    val displayName: String,
    val createdAt: Long,
    val tabSortOrder: Int,
    val scrollPosition: Int
)
