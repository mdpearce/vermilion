package com.neaniesoft.vermilion.postdetails.data

import androidx.core.net.toUri
import androidx.room.withTransaction
import com.neaniesoft.vermilion.api.entities.CommentData
import com.neaniesoft.vermilion.api.entities.CommentThing
import com.neaniesoft.vermilion.api.entities.Link
import com.neaniesoft.vermilion.api.entities.MoreCommentsData
import com.neaniesoft.vermilion.api.entities.MoreCommentsThing
import com.neaniesoft.vermilion.api.entities.ThingData
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.dbentities.comments.CommentRecord
import com.neaniesoft.vermilion.postdetails.data.http.CommentApiService
import com.neaniesoft.vermilion.postdetails.data.http.entities.CommentResponse
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlags
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.MoreCommentsCount
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    suspend fun getFlattenedCommentTreeForPost(
        postId: PostId
    ): Flow<CommentRepositoryResponse>

    suspend fun fetchAndInsertMoreCommentsFor(stub: CommentStub): List<CommentKind>
    suspend fun cachedCommentsForPost(postId: PostId): Int
}

sealed class CommentRepositoryResponse {
    data class ListOfComments(val comments: List<CommentKind>) : CommentRepositoryResponse()
    data class UpdatedPost(val post: Post) : CommentRepositoryResponse()
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
        private val CACHE_VALID_DURATION = Duration.ofMinutes(60)
    }

    private val logger by logger()
    override suspend fun getFlattenedCommentTreeForPost(
        postId: PostId
    ): Flow<CommentRepositoryResponse> =
        flow {
            val lastInsertedAtTime = database.withTransaction {
                dao.getLastInsertedAtForPost(postId.value)
            }
            val commentCount = database.withTransaction {
                dao.commentCountForPost(postId.value)
            }

            if (commentCount > 0 && (lastInsertedAtTime != null && lastInsertedAtTime >= clock.millis() - CACHE_VALID_DURATION.toMillis())) {
                // Cache is valid, let's just return the database records
                emit(
                    CommentRepositoryResponse.ListOfComments(
                        dao.getAllForPost(postId.value)
                            .map { it.toCommentKind() }
                    )
                )
            } else {
                // Cache is invalid. First, let's return it so we have something to display
                emit(
                    CommentRepositoryResponse.ListOfComments(
                        dao.getAllForPost(postId.value)
                            .map { it.toCommentKind() }
                    )
                )

                // Then, fetch new comments from the API
                val apiResponse = apiService.commentsForArticle(postId.value)

                // The response comes with an updated post, so we might as well return it so the caller can update the db if required
                val post = (apiResponse[0].data.children.firstOrNull()?.data as? Link)?.toPost(
                    markdownParser
                )
                if (post != null) {
                    emit(CommentRepositoryResponse.UpdatedPost(post))
                }

                val newCommentRecords: List<CommentRecord> =
                    apiResponse[1].getCommentRecords(postId)

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
                emit(CommentRepositoryResponse.ListOfComments(newComments.map { it.toCommentKind() }))
            }
        }

    override suspend fun fetchAndInsertMoreCommentsFor(stub: CommentStub): List<CommentKind> =
        coroutineScope {
            val moreCommentsResponse = async {
                apiService.moreChildren(
                    "json",
                    stub.children.map { it.value }.joinToString(",") { it },
                    false,
                    "t3_${stub.postId.value}",
                    "confidence"
                )
            }

            database.withTransaction {
                // First, cache all the existing comment entries in memory
                val allComments = dao.getAllForPost(stub.postId.value)
                logger.debugIfEnabled { "Loaded ${allComments.size} records from database" }

                // Then, delete all the entries in the db *after* that
                val stubId = dao.getIdForComment(stub.id.value)
                    ?: throw IllegalStateException("Could not find comment with ID ${stub.id.value} in db")
                logger.debugIfEnabled { "Deleting all from comment stub ID: $stubId" }
                dao.deleteAllFromId(stubId)

                // Now, insert the newly fetched comments in place of the stub
                val moreComments = moreCommentsResponse.await().json.data.things
                logger.debugIfEnabled { "Loaded ${moreComments.size} new comments from api, inserting into db" }
                dao.insertAll(
                    moreComments.map {
                        it.data.buildCommentRecord(stub.postId)
                    }
                )

                logger.debugIfEnabled { "Inserting remaining comments" }
                // Finally, insert all the comments after the stub to recreate the complete list.
                dao.insertAll(
                    allComments.subList(
                        allComments.indexOfFirst { it.commentId == stub.id.value } + 1,
                        allComments.size - 1
                    ).map { it.copy(id = 0) }
                )

                dao.getAllForPost(stub.postId.value)
            }.map { it.toCommentKind() }
        }

    override suspend fun cachedCommentsForPost(postId: PostId): Int {
        return database.withTransaction {
            dao.commentCountForPost(postId.value)
        }
    }

    private fun CommentRecord.toCommentKind(): CommentKind {
        return if (flags == CommentFlags.MORE_COMMENTS_STUB.name) {
            CommentKind.Stub(this.toCommentStub())
        } else {
            CommentKind.Full(this.toComment())
        }
    }

    private fun CommentRecord.toCommentStub(): CommentStub {
        return CommentStub(
            postId = PostId(postId),
            id = CommentId(commentId),
            count = MoreCommentsCount(score), // TODO stop using score as a proxy field for child count
            parentId = parentId?.let { CommentId(it) },
            depth = CommentDepth(depth),
            children = body.split(",").map { CommentId((it)) }
        )
    }

    private fun CommentRecord.toComment(): Comment {
        return Comment(
            id = CommentId(commentId),
            content = CommentContent(body),
            contentMarkdown = markdownParser.parse(body),
            flags = getFlags(),
            authorName = AuthorName(author),
            createdAt = Instant.ofEpochMilli(createdAt),
            createdAtDurationString = createdAt.formatDuration(),
            score = Score(score),
            link = link.toUri(),
            postId = PostId(postId),
            controversialIndex = ControversialIndex(controversialIndex),
            depth = CommentDepth(depth),
            upVotes = UpVotesCount(upVotes),
            parentId = parentId?.let { CommentId(it) }
        )
    }

    private fun Long.formatDuration(): DurationString {
        return DurationString(prettyTime.format(Instant.ofEpochSecond(this)))
    }

    private fun CommentRecord.getFlags(): Set<CommentFlags> {
        return flags.split(",")
            .filter { it.isNotEmpty() }
            .map { CommentFlags.valueOf(it) }
            .toSet()
    }

    private fun CommentResponse.getCommentRecords(postId: PostId): List<CommentRecord> {
        val comments = this.data.children.mapNotNull {
            when (it) {
                is CommentThing -> CommentOrStubData.Comment(it.data)
                is MoreCommentsThing -> CommentOrStubData.Stub(it.data)
                else -> null
            }
        }.flatMap {
            when (it) {
                is CommentOrStubData.Comment -> {
                    it.comment.children()
                }
                is CommentOrStubData.Stub -> {
                    listOf(it.moreComments)
                }
            }
        }.map { it.buildCommentRecord(postId) }
        return comments
    }

    private fun ThingData.buildCommentRecord(postId: PostId): CommentRecord {
        return when (this) {
            is CommentData -> this.toCommentRecord()
            is MoreCommentsData -> this.toCommentRecord(postId)
            else -> {
                throw IllegalStateException("Trying to build a comment record from a Thing which isn't a comment")
            }
        }
    }

    sealed class CommentOrStubData {
        data class Comment(val comment: CommentData) : CommentOrStubData()
        data class Stub(val moreComments: MoreCommentsData) : CommentOrStubData()
    }

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
        }
    }

    private fun MoreCommentsData.toCommentRecord(postId: PostId): CommentRecord {
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

    private fun CommentData.toCommentRecord(): CommentRecord {
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
}
