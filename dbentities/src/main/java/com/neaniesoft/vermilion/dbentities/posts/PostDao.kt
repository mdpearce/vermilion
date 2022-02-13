package com.neaniesoft.vermilion.dbentities.posts

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PostDao {
    @Insert
    suspend fun insertAll(posts: List<PostRecord>)

    @Query("SELECT * FROM posts WHERE `query` == :query")
    fun pagingSource(query: String): PagingSource<Int, PostRecord>

    @Query("DELETE FROM posts WHERE `query` == :query")
    suspend fun deleteByQuery(query: String)

    @Query("SELECT insertedAt FROM posts WHERE `query` == :query ORDER BY insertedAt DESC LIMIT 1")
    suspend fun lastUpdatedAt(query: String): Long
}
