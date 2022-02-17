package com.neaniesoft.vermilion.postdetails.data

import androidx.core.net.toUri
import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.CommentData
import com.neaniesoft.vermilion.api.entities.CommentThing
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.dbentities.comments.CommentRecord
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlags
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.postdetails.data.http.CommentApiService
import com.neaniesoft.vermilion.postdetails.data.http.CommentResponse
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.utils.logger
import org.ocpsoft.prettytime.PrettyTime
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
    private val clock: Clock,
    private val prettyTime: PrettyTime
) : CommentRepository {
    private val logger by logger()
    override suspend fun getFlattenedCommentTreeForPost(postId: PostId): List<Comment> {
        val apiResponse = apiService.commentsForArticle(postId.value)

        val commentRecords: List<CommentRecord> = apiResponse[1].getCommentRecords(clock)

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

        return comments.map { it.toComment(prettyTime) }
    }
}

private fun CommentRecord.toComment(prettyTime: PrettyTime): Comment {
    return Comment(
        id = CommentId(commentId),
        content = CommentContent(body),
        flags = getFlags(),
        authorName = AuthorName(author),
        createdAt = Instant.ofEpochMilli(createdAt),
        createdAtDurationString = createdAt.formatDuration(prettyTime),
        score = Score(score),
        link = link.toUri(),
        postId = PostId(postId),
        controversialIndex = ControversialIndex(controversialIndex),
        depth = CommentDepth(depth),
        upVotes = UpVotesCount(upVotes),
        parentId = parentId?.let { CommentId(it) }
    )
}

private fun Long.formatDuration(prettyTime: PrettyTime): DurationString {
    return DurationString(prettyTime.format(Instant.ofEpochSecond(this)))
}

private fun CommentRecord.getFlags(): Set<CommentFlags> {
    return flags.split(",")
        .filter { it.isNotEmpty() }
        .map { CommentFlags.valueOf(it) }
        .toSet()
}

private fun CommentResponse.getCommentRecords(clock: Clock): List<CommentRecord> {
    val comments = this.data.children.mapNotNull {
        (it as? CommentThing)?.data
    }.flatMap { it.children() }
        .map { it.toCommentRecord(clock) }
    return comments
}

// Naive depth-first traversal of the comment tree
private fun CommentData.children(): List<CommentData> {
    val children = replies?.data?.children

    return if (children.isNullOrEmpty()) {
        listOf(this)
    } else {
        listOf(this) + children.filterIsInstance<CommentThing>().flatMap {
            it.data.children()
        }
    }
}

private fun CommentData.toCommentRecord(clock: Clock): CommentRecord {
    val flags = listOfNotNull(
        if (saved) {
            CommentFlags.SAVED
        } else {
            null
        },
        if (edited > 0) {
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

