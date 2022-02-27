package com.neaniesoft.vermilion.dbentities.communities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CommunityDao {
    @Query("SELECT * from communities WHERE isSubscribed == 1 ORDER BY name ASC")
    suspend fun getAllSubscribedCommunities(): List<CommunityRecord>

    @Query("DELETE FROM communities")
    suspend fun removeAllCommunities()

    @Insert
    suspend fun insertAll(communities: List<CommunityRecord>)
}
