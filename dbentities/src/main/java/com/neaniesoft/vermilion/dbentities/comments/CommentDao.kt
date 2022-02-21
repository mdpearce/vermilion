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

    @Query("SELECT insertedAt FROM comments WHERE postId == :postId ORDER BY insertedAt DESC LIMIT 1")
    suspend fun getLastInsertedAtForPost(postId: String): Long?

    @Query("SELECT path FROM comments WHERE id == :commentId")
    suspend fun getPathForComment(commentId: String): String?

    @Insert
    suspend fun insertAll(vararg comments: CommentRecord)

    @Insert
    suspend fun insertAll(comments: List<CommentRecord>)

    @Update
    suspend fun update(comment: CommentRecord): Int

    @Query("DELETE FROM comments WHERE postId == :postId")
    suspend fun deleteAllForPost(postId: String): Int

    @Query("DELETE FROM comments WHERE id >= :id")
    suspend fun deleteAllFromId(id: Int)

    @Query("SELECT id FROM comments WHERE commentId == :commentId LIMIT 1")
    suspend fun getIdForComment(commentId: String): Int?
}
