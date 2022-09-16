package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.db.PostHistoryQueries
import com.neaniesoft.vermilion.posts.data.PostHistoryRepository
import com.neaniesoft.vermilion.posts.domain.entities.PostHistoryEntry
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostHistoryRoomRepository @Inject constructor(
    private val queries: PostHistoryQueries
) : PostHistoryRepository {
    override suspend fun addHistoryEntry(entry: PostHistoryEntry) {
        queries.insert(null, entry.postId.value, entry.viewedAt.toEpochMilli())
    }

    override suspend fun allEntries(): List<PostHistoryEntry> {
        return queries.historyRecordsByDate { id, post_id, visited_at ->
            PostHistoryEntry(PostId((post_id)), Instant.ofEpochMilli(visited_at))
        }.executeAsList()
    }
}
