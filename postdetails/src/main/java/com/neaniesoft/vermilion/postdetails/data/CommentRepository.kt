package com.neaniesoft.vermilion.postdetails.data

import android.graphics.Color
import androidx.core.net.toUri
import com.neaniesoft.vermilion.api.entities.CommentData
import com.neaniesoft.vermilion.api.entities.CommentThing
import com.neaniesoft.vermilion.api.entities.Link
import com.neaniesoft.vermilion.api.entities.MoreCommentsData
import com.neaniesoft.vermilion.api.entities.MoreCommentsThing
import com.neaniesoft.vermilion.api.entities.ThingData
import com.neaniesoft.vermilion.db.CommentQueries
import com.neaniesoft.vermilion.postdetails.data.http.CommentApiService
import com.neaniesoft.vermilion.postdetails.data.http.entities.CommentResponse
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlags
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlair
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairBackgroundColor
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairText
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairTextColor
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.MoreCommentsCount
import com.neaniesoft.vermilion.postdetails.domain.entities.ThreadStub
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.postdetails.domain.entities.fullName
import com.neaniesoft.vermilion.postdetails.domain.entities.isDownVoted
import com.neaniesoft.vermilion.postdetails.domain.entities.isUpVoted
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.utils.CoroutinesModule
import com.neaniesoft.vermilion.utils.formatCompact
import com.neaniesoft.vermilion.utils.logger
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import org.commonmark.parser.Parser
import retrofit2.HttpException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface CommentRepository {
    suspend fun getCommentsForPost(
        postId: PostId
    ): Flow<List<CommentKind>>

    suspend fun getCommentThread(
        postId: PostId,
        commentId: CommentId
    ): Flow<List<CommentKind>>

    suspend fun refreshThread(
        postId: PostId,
        threadId: CommentId,
        networkActivityIdentifier: String
    )

    suspend fun fetchAndInsertMoreCommentsFor(stub: CommentStub): List<CommentKind>
    suspend fun cachedCommentsForPost(postId: PostId): Int
    suspend fun deleteByPost(postId: PostId)
    suspend fun refreshIfRequired(
        postId: PostId,
        forceRefresh: Boolean,
        networkActivityIdentifier: String
    )

    val networkActivityUpdates: SharedFlow<NetworkActivityUpdate>

    suspend fun toggleUpVote(comment: Comment)
    suspend fun toggleDownVote(comment: Comment)
}

data class NetworkActivityUpdate(
    val identifier: String,
    val isActive: Boolean
)

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val apiService: CommentApiService,
    private val queries: CommentQueries,
    private val clock: Clock,
    private val markdownParser: Parser,
    private val postRepository: PostRepository,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : CommentRepository {

    companion object {
        private val CACHE_VALID_DURATION = Duration.ofMinutes(60)
    }

    private val logger by logger()

    private val _networkActivityUpdates: MutableSharedFlow<NetworkActivityUpdate> =
        MutableSharedFlow()
    override val networkActivityUpdates = _networkActivityUpdates.asSharedFlow()

    override suspend fun toggleUpVote(comment: Comment) {
        if (comment.isUpVoted()) {
            vote(0, comment)
        } else {
            vote(1, comment)
        }
    }

    private suspend fun vote(direction: Int, comment: Comment) {
        val flags = when (direction) {
            -1 -> comment.flags + CommentFlags.DOWN_VOTED - CommentFlags.UP_VOTED
            0 -> comment.flags - CommentFlags.UP_VOTED - CommentFlags.DOWN_VOTED
            1 -> comment.flags + CommentFlags.UP_VOTED - CommentFlags.DOWN_VOTED
            else -> throw IllegalArgumentException("Unexpected vote direction $direction")
        }
        withContext(dispatcher) {
            queries.updateFlags(
                flags = flags.joinToString(",") { it.name },
                commentId = comment.id.value
            )
            try {
                apiService.vote(direction, comment.id.fullName())
            } catch (httpException: HttpException) {
                logger.errorIfEnabled(httpException) { "HTTP Exception while processing vote" }
            }
        }
    }

    override suspend fun toggleDownVote(comment: Comment) {
        if (comment.isDownVoted()) {
            vote(0, comment)
        } else {
            vote(-1, comment)
        }
    }

    override suspend fun getCommentsForPost(postId: PostId): Flow<List<CommentKind>> {
        return queries.selectAllForPost(
            postId = postId.value,
            mapper = commentMapper
        ).asFlow().mapToList()
    }

    override suspend fun getCommentThread(
        postId: PostId,
        commentId: CommentId
    ): Flow<List<CommentKind>> {
        return queries.selectCommentThread(postId.value, commentId.value, commentMapper).asFlow()
            .mapToList()
    }

    override suspend fun refreshThread(
        postId: PostId,
        threadId: CommentId,
        networkActivityIdentifier: String
    ) {
        _networkActivityUpdates.emit(NetworkActivityUpdate(networkActivityIdentifier, true))

        val apiResponse = apiService.commentThread(postId.value, threadId.value)
        val post = (apiResponse[0].data.children.firstOrNull()?.data as? Link)?.toPost(
            markdownParser
        )
        if (post != null) {
            postRepository.update(postId, post)
        }

        val newCommentRecords: List<com.neaniesoft.vermilion.db.Comment> =
            apiResponse[1].getComments(postId, threadId)

        queries.transaction {
            queries.deleteAllForThread(postId.value, threadId.value)
            newCommentRecords.forEach { record ->
                queries.insert(
                    null,
                    comment_id = record.comment_id,
                    post_id = record.post_id,
                    parent_id = record.parent_id,
                    path = record.path,
                    body = record.body,
                    flags = record.flags,
                    author = record.author,
                    created_at = record.created_at,
                    edited_at = record.edited_at,
                    inserted_at = record.inserted_at,
                    score = record.score,
                    link = record.link,
                    controversial_index = record.controversial_index,
                    depth = record.depth,
                    up_votes = record.up_votes,
                    flair_text = record.flair_text,
                    flair_background_color = record.flair_background_color,
                    flair_text_color = record.flair_text_color,
                    thread_identifier = record.thread_identifier
                )
            }
        }

        _networkActivityUpdates.emit(NetworkActivityUpdate(networkActivityIdentifier, false))
    }

    override suspend fun refreshIfRequired(
        postId: PostId,
        forceRefresh: Boolean,
        networkActivityIdentifier: String
    ) {
        val lastInsertedAtTime =
            queries.selectLastInsertedAtForPost(postId.value).executeAsOneOrNull() ?: 0
        val commentCount = queries.selectCommentCountForPost(postId.value).executeAsOne()
        val cacheIsValid = lastInsertedAtTime >= clock.millis() - CACHE_VALID_DURATION.toMillis()
        if (forceRefresh || commentCount == 0L || !cacheIsValid) {
            logger.debugIfEnabled { "Refreshing comments for ${postId.value} (forceRefresh == $forceRefresh)" }
            _networkActivityUpdates.emit(NetworkActivityUpdate(networkActivityIdentifier, true))
            // Fetch new comments
            val apiResponse = apiService.commentsForArticle(postId.value)

            // The response comes with an updated post, so we might as well update it in the db
            val post = (apiResponse[0].data.children.firstOrNull()?.data as? Link)?.toPost(
                markdownParser
            )
            if (post != null) {
                postRepository.update(postId, post)
            }
            val newCommentRecords: List<com.neaniesoft.vermilion.db.Comment> =
                apiResponse[1].getComments(postId)

            // Finally, delete the old entries and insert the new ones and then return the new ones from the DAO
            queries.transaction {
                queries.deleteAllForPost(postId.value)
                newCommentRecords.forEach { record ->
                    queries.insert(
                        null,
                        comment_id = record.comment_id,
                        post_id = record.post_id,
                        parent_id = record.parent_id,
                        path = record.path,
                        body = record.body,
                        flags = record.flags,
                        author = record.author,
                        created_at = record.created_at,
                        edited_at = record.edited_at,
                        inserted_at = record.inserted_at,
                        score = record.score,
                        link = record.link,
                        controversial_index = record.controversial_index,
                        depth = record.depth,
                        up_votes = record.up_votes,
                        flair_text = record.flair_text,
                        flair_background_color = record.flair_background_color,
                        flair_text_color = record.flair_text_color,
                        thread_identifier = record.thread_identifier
                    )
                }
            }
            _networkActivityUpdates.emit(NetworkActivityUpdate(networkActivityIdentifier, false))
        } else {
            logger.debugIfEnabled { "Cache up to date, not refreshing." }
        }
    }

    override suspend fun fetchAndInsertMoreCommentsFor(stub: CommentStub): List<CommentKind> =
        coroutineScope {
            val moreCommentsResponse =
                apiService.moreChildren(
                    "json",
                    stub.children.map { it.value }.joinToString(",") { it },
                    false,
                    "t3_${stub.postId.value}",
                    "confidence"
                )

            queries.transactionWithResult {
                // First, cache all the existing comment entries in memory
                val allComments = queries.selectAllForPost(stub.postId.value).executeAsList()
                logger.debugIfEnabled { "Loaded ${allComments.size} records from database" }

                // Then, delete all the entries in the db *after* that
                val stubId = queries.selectIdForComment(stub.id.value).executeAsOneOrNull()
                    ?: throw IllegalStateException("Could not find comment with ID ${stub.id.value} in db")
                logger.debugIfEnabled { "Deleting all from comment stub ID: $stubId" }
                queries.deleteAllFromId(stubId)

                // Now, insert the newly fetched comments in place of the stub
                val moreComments = moreCommentsResponse.json.data.things
                logger.debugIfEnabled { "Loaded ${moreComments.size} new comments from api, inserting into db" }
                moreComments.map { it.data.buildComment(stub.postId) }.forEach { record ->
                    queries.insert(
                        null,
                        comment_id = record.comment_id,
                        post_id = record.post_id,
                        parent_id = record.parent_id,
                        path = record.path,
                        body = record.body,
                        flags = record.flags,
                        author = record.author,
                        created_at = record.created_at,
                        edited_at = record.edited_at,
                        inserted_at = record.inserted_at,
                        score = record.score,
                        link = record.link,
                        controversial_index = record.controversial_index,
                        depth = record.depth,
                        up_votes = record.up_votes,
                        flair_text = record.flair_text,
                        flair_background_color = record.flair_background_color,
                        flair_text_color = record.flair_text_color,
                        thread_identifier = record.thread_identifier
                    )
                }

                logger.debugIfEnabled { "Inserting remaining comments" }
                // Finally, insert all the comments after the stub to recreate the complete list.
                val indexOfStub = allComments.indexOfFirst { it.comment_id == stub.id.value }
                if (indexOfStub < allComments.lastIndex) {
                    allComments.subList(
                        indexOfStub + 1,
                        allComments.size - 1
                    ).forEach { record ->
                        queries.insert(
                            null,
                            comment_id = record.comment_id,
                            post_id = record.post_id,
                            parent_id = record.parent_id,
                            path = record.path,
                            body = record.body,
                            flags = record.flags,
                            author = record.author,
                            created_at = record.created_at,
                            edited_at = record.edited_at,
                            inserted_at = record.inserted_at,
                            score = record.score,
                            link = record.link,
                            controversial_index = record.controversial_index,
                            depth = record.depth,
                            up_votes = record.up_votes,
                            flair_text = record.flair_text,
                            flair_background_color = record.flair_background_color,
                            flair_text_color = record.flair_text_color,
                            thread_identifier = record.thread_identifier
                        )
                    }
                }

                queries.selectAllForPost(stub.postId.value, mapper = commentMapper).executeAsList()
            }
        }

    override suspend fun cachedCommentsForPost(postId: PostId): Int {
        return queries.selectCommentCountForPost(postId.value).executeAsOneOrNull()?.toInt() ?: 0
    }

    override suspend fun deleteByPost(postId: PostId) {
        queries.deleteAllForPost(postId.value)
    }

    private val commentMapper: (
        id: Long,
        comment_id: String,
        post_id: String,
        parent_id: String?,
        path: String?,
        body: String,
        flags: String,
        author: String,
        created_at: Long,
        edited_at: Long,
        inserted_at: Long,
        score: Long,
        link: String,
        controversial_index: Long,
        depth: Long,
        up_votes: Long,
        flair_text: String?,
        flair_background_color: Long,
        flair_text_color: String,
        thread_identifier: String?
    ) -> CommentKind = { id: Long,
        comment_id: String,
        post_id: String,
        parent_id: String?,
        path: String?,
        body: String,
        flags: String,
        author: String,
        created_at: Long,
        edited_at: Long,
        inserted_at: Long,
        score: Long,
        link: String,
        controversial_index: Long,
        depth: Long,
        up_votes: Long,
        flair_text: String?,
        flair_background_color: Long,
        flair_text_color: String,
        thread_identifier: String? ->

        if (flags == CommentFlags.MORE_COMMENTS_STUB.name) {
            if (score == 0L) {
                CommentKind.Thread(
                    ThreadStub(
                        postId = PostId(post_id),
                        parentId = CommentId(
                            requireNotNull(parent_id).replace(
                                "t1_",
                                ""
                            )
                        ), // lol const
                        depth = CommentDepth(depth.toInt())
                    )
                )
            } else {
                CommentKind.Stub(
                    CommentStub(
                        postId = PostId(post_id),
                        id = CommentId(comment_id),
                        count = MoreCommentsCount(score.toInt()), // TODO stop using score as a proxy field for child count
                        parentId = parent_id?.let { CommentId(it) },
                        depth = CommentDepth(depth.toInt()),
                        children = body.split(",").map { CommentId((it)) }
                    )
                )
            }
        } else {
            CommentKind.Full(
                Comment(
                    id = CommentId(comment_id),
                    content = CommentContent(body),
                    contentMarkdown = markdownParser.parse(body),
                    flags = flags.getFlags(),
                    authorName = AuthorName(author),
                    createdAt = Instant.ofEpochMilli(created_at),
                    createdAtDurationString = created_at.formatDuration(),
                    editedAt = if (edited_at > 0) {
                        Instant.ofEpochMilli(edited_at)
                    } else {
                        null
                    },
                    editedAtDurationString = if (edited_at > 0) {
                        edited_at.formatDuration()
                    } else {
                        null
                    },
                    score = Score(score.toInt()),
                    link = link.toUri(),
                    postId = PostId(post_id),
                    controversialIndex = ControversialIndex(controversial_index.toInt()),
                    depth = CommentDepth(depth.toInt()),
                    upVotes = UpVotesCount(up_votes.toInt()),
                    parentId = parent_id?.let { CommentId(it) },
                    commentFlair = flair(
                        flair_text,
                        flair_text_color,
                        flair_background_color.toInt()
                    )
                )
            )
        }
    }

    private fun flair(
        flairText: String?,
        flairTextColor: String,
        flairBackgroundColor: Int
    ): CommentFlair {
        return if (flairText.isNullOrEmpty()) {
            CommentFlair.NoFlair
        } else {
            CommentFlair.TextFlair(
                CommentFlairText(flairText),
                CommentFlairBackgroundColor(flairBackgroundColor),
                CommentFlairTextColor.valueOf(flairTextColor.uppercase())
            )
        }
    }

    private fun Long.formatDuration(): DurationString {
        return DurationString(
            (clock.millis() - (this * 1000)).toDuration(DurationUnit.MILLISECONDS).formatCompact()
        )
    }

    private fun String.getFlags(): Set<CommentFlags> {
        return this.split(",")
            .filter { it.isNotEmpty() }
            .map { CommentFlags.valueOf(it) }
            .toSet()
    }

    private fun CommentResponse.getComments(
        postId: PostId,
        threadId: CommentId? = null
    ): List<com.neaniesoft.vermilion.db.Comment> {
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
        }.map { it.buildComment(postId, threadId) }
        return comments
    }

    private fun ThingData.buildComment(
        postId: PostId,
        threadId: CommentId? = null
    ): com.neaniesoft.vermilion.db.Comment {
        return when (this) {
            is CommentData -> this.toCommentRecord(threadId = threadId)
            is MoreCommentsData -> this.toCommentRecord(postId, threadId)
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

    private fun MoreCommentsData.toCommentRecord(
        postId: PostId,
        threadId: CommentId? = null
    ): com.neaniesoft.vermilion.db.Comment {
        val flags = listOf(CommentFlags.MORE_COMMENTS_STUB)
        val now = clock.millis()

        return com.neaniesoft.vermilion.db.Comment(
            id = 0,
            comment_id = id,
            post_id = postId.value,
            parent_id = parentId,
            path = null,
            body = children.joinToString(",") { it }, // TODO use a proper field for storing children.
            flags = flags.joinToString(",") { it.name },
            author = "",
            created_at = now,
            inserted_at = now,
            edited_at = 0,
            score = count.toLong(), // TODO this is pretty gross, using the score field to store the child count. Should probably modify the schema to allow for this new field instead.
            link = "",
            controversial_index = 0,
            depth = depth.toLong(),
            up_votes = 0,
            flair_text = null,
            flair_background_color = 0,
            flair_text_color = CommentFlairTextColor.DARK.name,
            thread_identifier = threadId?.value
        )
    }

    private fun CommentData.toCommentRecord(threadId: CommentId? = null): com.neaniesoft.vermilion.db.Comment {
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
            when (distinguished) {
                "moderator" -> CommentFlags.IS_MOD
                "admin" -> CommentFlags.IS_ADMIN
                else -> null
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
            },
            when (likes) {
                true -> {
                    CommentFlags.UP_VOTED
                }
                false -> {
                    (CommentFlags.DOWN_VOTED)
                }
                else -> {
                    null
                }
            }
        ).joinToString(",") { it.name }

        return com.neaniesoft.vermilion.db.Comment(
            id = 0,
            comment_id = id,
            post_id = linkId.replace("t3_", ""),
            parent_id = parentId.takeIf { !it.startsWith("t3_") }?.replace("t1_", ""),
            path = null,
            body = StringEscapeUtils.unescapeHtml4(body),
            flags = flags,
            author = author,
            created_at = createdUtc.toLong(),
            edited_at = edited.toLong(),
            inserted_at = clock.millis(),
            score = score.toLong(),
            link = permalink,
            controversial_index = controversiality.toLong(),
            depth = depth.toLong(),
            up_votes = ups.toLong(),
            flair_text = authorFlairText,
            flair_background_color = flairBackgroundColor().toLong(),
            flair_text_color = flairTextColor(),
            thread_identifier = threadId?.value
        )
    }

    private fun CommentData.flairBackgroundColor(): Int {
        return if (!authorFlairBackgroundColor.isNullOrEmpty()) {
            if (authorFlairBackgroundColor == "transparent") {
                0
            } else {
                Color.parseColor(authorFlairBackgroundColor)
            }
        } else {
            0
        }
    }

    private fun CommentData.flairTextColor(): String {
        return authorFlairTextColor?.uppercase() ?: CommentFlairTextColor.DARK.name
    }
}
