package com.neaniesoft.vermilion.dbentities.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostHistoryDao {

    @Insert
    suspend fun insertAll(vararg records: PostHistoryRecord)

    @Query("SELECT * FROM post_history ORDER BY visitedAt DESC")
    suspend fun getAllRecordsByDate(): List<PostHistoryRecord>
}
