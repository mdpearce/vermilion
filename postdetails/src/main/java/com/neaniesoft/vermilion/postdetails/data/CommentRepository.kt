package com.neaniesoft.vermilion.postdetails.data

import androidx.core.net.toUri
import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.CommentData
import com.neaniesoft.vermilion.api.entities.CommentThing
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.dbentities.comments.CommentRecord
import com.neaniesoft.vermilion.postdetails.Comment
import com.neaniesoft.vermilion.postdetails.CommentContent
import com.neaniesoft.vermilion.postdetails.CommentDepth
import com.neaniesoft.vermilion.postdetails.CommentFlags
import com.neaniesoft.vermilion.postdetails.CommentId
import com.neaniesoft.vermilion.postdetails.ControversialIndex
import com.neaniesoft.vermilion.postdetails.UpVotesCount
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface CommentRepository {
    suspend fun getFlattenedCommentTreeForPost(postId: PostId): List<Comment>
}

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val apiService: CommentApiService,
    private val database: VermilionDatabase,
    private val dao: CommentDao,
    private val clock: Clock
) : CommentRepository {
    private val logger by logger()
    override suspend fun getFlattenedCommentTreeForPost(postId: PostId): List<Comment> {
        val apiResponse = apiService.commentsForArticle(postId.value)

        val commentRecords: List<CommentRecord> = apiResponse.getCommentRecords(clock)

        val comments = database.withTransaction {
            dao.deleteAllForPost(postId.value)

            commentRecords.forEach { record ->
                dao.insertAll(record)
                val parentId = record.parentId
                val parentPath = if (parentId != null) {
                    dao.getPathForComment(parentId) + "/"
                } else {
                    ""
                }
                val updatedRecord = record.copy(path = "$parentPath${record.id}")
                dao.update(updatedRecord)
            }

            dao.getAllForPost(postId.value)
        }

        return comments.map { it.toComment() }
    }
}

fun CommentRecord.toComment(): Comment {
    return Comment(
        id = CommentId(commentId),
        content = CommentContent(body),
        flags = getFlags(),
        authorName = AuthorName(author),
        createdAt = Instant.ofEpochMilli(createdAt),
        score = Score(score),
        link = link.toUri(),
        postId = PostId(postId),
        controversialIndex = ControversialIndex(controversialIndex),
        depth = CommentDepth(depth),
        upVotes = UpVotesCount(upVotes),
        parentId = parentId?.let { CommentId(it) }
    )
}

fun CommentRecord.getFlags(): Set<CommentFlags> {
    return flags.split(",")
        .map { CommentFlags.valueOf(it) }
        .toSet()
}

fun CommentResponse.getCommentRecords(clock: Clock): List<CommentRecord> {
    val comments = this.data.children.mapNotNull {
        (it as? CommentThing)?.data
    }.flatMap { it.children() }
        .map { it.toCommentRecord(clock) }
    return comments
}

fun CommentData.children(): List<CommentData> {
    return if (replies.children.isEmpty()) {
        listOf(this)
    } else {
        replies.children.flatMap {
            (it as? CommentThing)?.data?.children()
                ?: throw IllegalStateException("Should not be null here")
        }
    }
}

fun CommentData.toCommentRecord(clock: Clock): CommentRecord {
    val flags = listOfNotNull<CommentFlags>(
        if (saved) {
            CommentFlags.SAVED
        } else {
            null
        },
        if (edited) {
            CommentFlags.EDITED
        } else {
            null
        },
        if (isSubmitter) {
            CommentFlags.IS_OP
        } else {
            null
        },
        if (sticked) {
            CommentFlags.STICKIED
        } else {
            null
        },
        if (scoreHidden) {
            CommentFlags.SCORE_HIDDEN
        } else {
            null
        },
        if (locked) {
            CommentFlags.LOCKED
        } else {
            null
        }
    ).joinToString(",") { it.name }

    return CommentRecord(
        id = 0,
        commentId = id,
        postId = linkId.replace("t3_", ""),
        parentId = parentId.takeIf { !it.startsWith("t3_") }?.replace("t1_", ""),
        path = null,
        body = body,
        flags = flags,
        author = author,
        createdAt = createdUtc.toLong(),
        insertedAt = clock.millis(),
        score = score,
        link = permalink,
        controversialIndex = controversiality,
        depth = depth,
        upVotes = ups
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CommentRepositoryModule {
    @Binds
    abstract fun provideCommentRepository(impl: CommentRepositoryImpl): CommentRepository
}
