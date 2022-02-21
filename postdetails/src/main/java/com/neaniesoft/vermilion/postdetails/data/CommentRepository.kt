package com.neaniesoft.vermilion.postdetails.data

import androidx.core.net.toUri
import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.CommentData
import com.neaniesoft.vermilion.api.entities.CommentThing
import com.neaniesoft.vermilion.api.entities.MoreCommentsData
import com.neaniesoft.vermilion.api.entities.MoreCommentsThing
import com.neaniesoft.vermilion.api.entities.ThingData
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.dbentities.comments.CommentRecord
import com.neaniesoft.vermilion.postdetails.data.http.CommentApiService
import com.neaniesoft.vermilion.postdetails.data.http.CommentResponse
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlags
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.FullComment
import com.neaniesoft.vermilion.postdetails.domain.entities.MoreCommentsCount
import com.neaniesoft.vermilion.postdetails.domain.entities.MoreCommentsStub
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.text.StringEscapeUtils
import org.commonmark.parser.Parser
import org.ocpsoft.prettytime.PrettyTime
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface CommentRepository {
    suspend fun getFlattenedCommentTreeForPost(postId: PostId): Flow<List<CommentKind>>
}

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val apiService: CommentApiService,
    private val database: VermilionDatabase,
    private val dao: CommentDao,
    private val clock: Clock,
    private val prettyTime: PrettyTime,
    private val markdownParser: Parser // TODO replace with a wrapper interface
) : CommentRepository {
    companion object {
        private val CACHE_VALID_DURATION = Duration.ofMinutes(1)
    }

    private val logger by logger()
    override suspend fun getFlattenedCommentTreeForPost(postId: PostId): Flow<List<CommentKind>> =
        flow {
            val lastInsertedAtTime = database.withTransaction {
                dao.getLastInsertedAtForPost(postId.value)
            }

            if (lastInsertedAtTime != null && lastInsertedAtTime >= clock.millis() - CACHE_VALID_DURATION.toMillis()) {
                // Cache is valid, let's just return the database records
                emit(
                    dao.getAllForPost(postId.value)
                        .map { it.toCommentKind(prettyTime, markdownParser) }
                )
            } else {
                // Cache is invalid. First, let's return it so we have something to display
                emit(
                    dao.getAllForPost(postId.value)
                        .map { it.toCommentKind(prettyTime, markdownParser) }
                )

                // Then, fetch new comments from the API
                val apiResponse = apiService.commentsForArticle(postId.value)
                val newCommentRecords: List<CommentRecord> =
                    apiResponse[1].getCommentRecords(postId, clock)

                // Finally, delete the old entries and insert the new ones and then return the new ones from the DAO
                val newComments = database.withTransaction {
                    dao.deleteAllForPost(postId.value)

                    newCommentRecords.forEach { record ->
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

                // Now, emit the new comments
                emit(newComments.map { it.toCommentKind(prettyTime, markdownParser) })
            }
        }
}

private fun CommentRecord.toCommentKind(prettyTime: PrettyTime, parser: Parser): CommentKind {
    return if (flags == CommentFlags.MORE_COMMENTS_STUB.name) {
        MoreCommentsStub(this.toCommentStub())
    } else {
        FullComment(this.toComment(prettyTime, parser))
    }
}

private fun CommentRecord.toCommentStub(): com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub {
    return com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub(
        id = CommentId(commentId),
        count = MoreCommentsCount(score), // TODO stop using score as a proxy field for child count
        parentId = parentId?.let { CommentId(it) },
        depth = CommentDepth(depth),
        children = body.split(",").map { CommentId((it)) }
    )
}

private fun CommentRecord.toComment(prettyTime: PrettyTime, parser: Parser): Comment {
    return Comment(
        id = CommentId(commentId),
        content = CommentContent(body),
        contentMarkdown = parser.parse(body),
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

private fun CommentResponse.getCommentRecords(postId: PostId, clock: Clock): List<CommentRecord> {
    val comments = this.data.children.mapNotNull {
        // (it as? CommentThing)?.data

        when (it) {
            is CommentThing -> CommentContainer(it.data)
            is MoreCommentsThing -> CommentStub(it.data)
            else -> null
        }

    }.flatMap {
        when (it) {
            is CommentContainer -> {
                it.comment.children()
            }
            is CommentStub -> {
                listOf(it.moreComments)
            }
        }
    }.map { it.buildCommentRecord(postId, clock) }
    return comments
}

private fun ThingData.buildCommentRecord(postId: PostId, clock: Clock): CommentRecord {
    return when (this) {
        is CommentData -> this.toCommentRecord(clock)
        is MoreCommentsData -> this.toCommentRecord(postId, clock)
        else -> {
            throw IllegalStateException("Trying to build a comment record from a Thing which isn't a comment")
        }
    }
}

sealed class CommentOrStub
data class CommentContainer(val comment: CommentData) : CommentOrStub()
data class CommentStub(val moreComments: MoreCommentsData) : CommentOrStub()

// Naive depth-first traversal of the comment tree
private fun CommentData.children(): List<ThingData> {
    val children = replies?.data?.children

    return if (children.isNullOrEmpty()) {
        listOf(this)
    } else {
        listOf(this) + children.flatMap { child ->
            when (child) {
                is CommentThing -> child.data.children()
                is MoreCommentsThing -> listOf(child.data)
                else -> null
            }
        }
        //
        //
        //
        //
        //     children.filterIsInstance<CommentThing>().flatMap {
        //     it.data.children()
        // }
    }
}

private fun MoreCommentsData.toCommentRecord(postId: PostId, clock: Clock): CommentRecord {
    val flags = listOf(CommentFlags.MORE_COMMENTS_STUB)
    val now = clock.millis()

    return CommentRecord(
        id = 0,
        commentId = id,
        postId = postId.value,
        parentId = parentId,
        path = null,
        body = children.joinToString(",") { it }, // TODO use a proper field for storing children.
        flags = flags.joinToString(",") { it.name },
        author = "",
        createdAt = now,
        insertedAt = now,
        score = count, // TODO this is pretty gross, using the score field to store the child count. Should probably modify the schema to allow for this new field instead.
        link = "",
        controversialIndex = 0,
        depth = depth,
        upVotes = 0
    )
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
        body = StringEscapeUtils.unescapeHtml4(body),
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
