package com.neaniesoft.vermilion.posts.data

import com.neaniesoft.vermilion.posts.domain.entities.PostHistoryEntry

interface PostHistoryRepository {
    suspend fun addHistoryEntry(entry: PostHistoryEntry)
    suspend fun allEntries(): List<PostHistoryEntry>
}
