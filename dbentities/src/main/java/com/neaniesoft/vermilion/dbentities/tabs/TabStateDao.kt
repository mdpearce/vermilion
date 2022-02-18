package com.neaniesoft.vermilion.dbentities.tabs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TabStateDao {
    @Query("SELECT * FROM tabs ORDER BY tabSortOrder ASC")
    fun getAllCurrentTabs(): Flow<List<TabStateRecord>>

    @Insert
    suspend fun insertAll(vararg tabs: TabStateRecord)

    @Query("DELETE FROM tabs WHERE id == :id")
    suspend fun deleteTabWithId(id: Int)
}
