package com.neaniesoft.vermilion.dbentities.comments

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * from comments WHERE postId == :postId")
    suspend fun getAllForPost(postId: String): List<CommentRecord>

    @Query("SELECT * from comments WHERE postId == :postId")
    fun flowOfCommentsForPost(postId: String): Flow<List<CommentRecord>>

    @Query("SELECT * from comments WHERE postId == :postId AND threadIdentifier == :commentId")
    fun flowOfCommentThread(postId: String, commentId: String): Flow<List<CommentRecord>>

    @Query("SELECT * from comments WHERE postId == :postId AND parentId == null")
    suspend fun getAllTopLevelRecordsForPost(postId: String): List<CommentRecord>

    @Query("SELECT * from comments WHERE path LIKE :commentPath || '%'")
    suspend fun getAllDescendantsOfCommentWithPath(commentPath: String): List<CommentRecord>

    @Query("SELECT insertedAt FROM comments WHERE postId == :postId ORDER BY insertedAt DESC LIMIT 1")
    suspend fun getLastInsertedAtForPost(postId: String): Long?

    @Query("SELECT path FROM comments WHERE commentId == :commentId")
    suspend fun getPathForComment(commentId: String): String?

    @Insert
    suspend fun insertAll(vararg comments: CommentRecord)

    @Insert
    suspend fun insertAll(comments: List<CommentRecord>)

    @Update
    suspend fun update(comment: CommentRecord): Int

    @Query("DELETE FROM comments WHERE postId == :postId")
    suspend fun deleteAllForPost(postId: String): Int

    @Query("DELETE FROM comments WHERE postId == :postId AND threadIdentifier == :threadId")
    suspend fun deleteAllForThread(postId: String, threadId: String): Int

    @Query("DELETE FROM comments WHERE id >= :id")
    suspend fun deleteAllFromId(id: Int)

    @Query("SELECT id FROM comments WHERE commentId == :commentId LIMIT 1")
    suspend fun getIdForComment(commentId: String): Int?

    @Query("SELECT count(id) FROM comments WHERE postId == :postId")
    suspend fun commentCountForPost(postId: String): Int

    @Query("UPDATE comments SET flags = :flags WHERE commentId == :commentId")
    suspend fun updateFlags(commentId: String, flags: String): Int
}
