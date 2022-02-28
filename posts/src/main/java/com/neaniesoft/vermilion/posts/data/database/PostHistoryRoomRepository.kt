package com.neaniesoft.vermilion.posts.data.database

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryDao
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryRecord
import com.neaniesoft.vermilion.posts.data.PostHistoryRepository
import com.neaniesoft.vermilion.posts.data.entities.PostHistoryEntry
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostHistoryRoomRepository @Inject constructor(
    private val db: VermilionDatabase,
    private val dao: PostHistoryDao,
    private val clock: Clock
) : PostHistoryRepository {
    override suspend fun addHistoryEntry(entry: PostHistoryEntry) {
        db.withTransaction {
            dao.insertAll(entry.toRecord())
        }
    }

    override suspend fun allEntries(): List<PostHistoryEntry> {
        return db.withTransaction {
            dao.getAllRecordsByDate()
        }.map { record ->
            record.toEntry()
        }
    }

    private fun PostHistoryRecord.toEntry(): PostHistoryEntry {
        return PostHistoryEntry(PostId(postId), Instant.ofEpochMilli(visitedAt))
    }

    private fun PostHistoryEntry.toRecord(): PostHistoryRecord {
        return PostHistoryRecord(0, postId.value, clock.millis())
    }
}


