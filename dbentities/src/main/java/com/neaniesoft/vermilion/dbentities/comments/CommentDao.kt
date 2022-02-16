package com.neaniesoft.vermilion.dbentities.comments

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CommentDao {
    @Query("SELECT * from comments WHERE postId == :postId")
    suspend fun getAllForPost(postId: String): List<CommentRecord>

    @Query("SELECT * from comments WHERE postId == :postId AND parentId == null")
    suspend fun getAllTopLevelRecordsForPost(postId: String): List<CommentRecord>

    @Query("SELECT * from comments WHERE path LIKE :commentPath || '%'")
    suspend fun getAllDescendantsOfCommentWithPath(commentPath: String): List<CommentRecord>

    @Insert
    suspend fun insertAll(vararg comments: CommentRecord)

    @Insert
    suspend fun insertAll(comments: List<CommentRecord>)

    @Update
    suspend fun update(comment: CommentRecord): Int

    @Query("DELETE FROM comments WHERE postId == :postId")
    suspend fun deleteAllForPost(postId: String): Int
}
