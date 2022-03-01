package com.neaniesoft.vermilion.posts.data.database

import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryDao
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryRecord
import com.neaniesoft.vermilion.posts.data.PostHistoryRepository
import com.neaniesoft.vermilion.posts.domain.entities.PostHistoryEntry
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostHistoryRoomRepository @Inject constructor(
    private val db: VermilionDatabase,
    private val dao: PostHistoryDao
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
        return PostHistoryRecord(0, postId.value, viewedAt.toEpochMilli())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PostHistoryRoomRepositoryModule {
    @Binds
    abstract fun providePostHistoryRepository(impl: PostHistoryRoomRepository): PostHistoryRepository
}
