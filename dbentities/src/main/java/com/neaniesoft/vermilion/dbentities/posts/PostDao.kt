package com.neaniesoft.vermilion.dbentities.posts

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert
    suspend fun insertAll(posts: List<PostRecord>)

    @Transaction
    @Query("SELECT * FROM posts WHERE `query` == :query")
    fun pagingSource(query: String): PagingSource<Int, PostWithHistory>

    @Query("DELETE FROM posts WHERE `query` == :query")
    suspend fun deleteByQuery(query: String)

    @Query("SELECT insertedAt FROM posts WHERE `query` == :query ORDER BY insertedAt DESC LIMIT 1")
    suspend fun lastUpdatedAt(query: String): Long?

    @Query("SELECT * from posts WHERE `postId` == :postId ORDER BY insertedAt DESC LIMIT 1")
    suspend fun postWithId(postId: String): PostRecord?

    @Query("SELECT * from posts WHERE `postId` == :postId ORDER BY insertedAt DESC LIMIT 1")
    fun postWithIdFlow(postId: String): Flow<PostRecord>

    @Query("DELETE FROM posts")
    suspend fun deleteAll()

    @Query("SELECT count(id) FROM posts WHERE `query` == :query")
    suspend fun postCount(query: String): Int

    @Query("UPDATE posts SET flags = :flags WHERE postId == :postId")
    suspend fun updateFlags(postId: String, flags: String): Int
}
