package com.neaniesoft.vermilion.dbentities.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: PostRemoteKey)

    @Query("SELECT * FROM post_remote_keys WHERE label = :query")
    suspend fun remoteKeyByQuery(query: String): PostRemoteKey

    @Query("DELETE FROM post_remote_keys WHERE label = :query")
    suspend fun deleteByQuery(query: String)
}
