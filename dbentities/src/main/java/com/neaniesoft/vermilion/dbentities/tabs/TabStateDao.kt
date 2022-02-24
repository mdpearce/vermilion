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

    @Insert(entity = TabStateRecord::class)
    suspend fun insertAll(vararg newTabs: NewTabStateRecord)

    @Query("DELETE FROM tabs WHERE id == :id")
    suspend fun deleteTabWithId(id: Int)

    @Query("SELECT * FROM tabs WHERE parentId == :parentId AND type == :type")
    suspend fun findByParentAndType(parentId: String, type: String): List<TabStateRecord>

    @Query("SELECT tabSortOrder FROM tabs ORDER BY tabSortOrder ASC LIMIT 1")
    suspend fun getLeftMostSortIndex(): Int?

    @Query("UPDATE tabs SET tabSortOrder = tabSortOrder + 1 WHERE tabSortOrder >= :from")
    suspend fun shiftAllTabsFrom(from: Int)

    @Query("DELETE FROM tabs")
    suspend fun deleteAll()

    @Query("UPDATE tabs SET scrollPosition = :scrollPosition WHERE parentId == :parentId AND type == :type")
    suspend fun updateTabWithScrollState(parentId: String, type: String, scrollPosition: Int)
}
