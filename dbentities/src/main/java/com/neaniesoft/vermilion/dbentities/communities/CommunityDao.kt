package com.neaniesoft.vermilion.dbentities.communities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {
    @Query("SELECT * from communities WHERE isSubscribed == 1 ORDER BY name ASC")
    fun observeAllSubscribedCommunities(): Flow<List<CommunityRecord>>

    @Query("DELETE FROM communities")
    suspend fun removeAllCommunities()

    @Insert
    suspend fun insertAll(communities: List<CommunityRecord>)
}
