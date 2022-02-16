package com.neaniesoft.vermilion.postdetails

import android.net.Uri
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import java.time.Instant

data class Comment(
    val id: CommentId,
    val content: CommentContent,
    val flags: Set<CommentFlags>,
    val authorName: AuthorName,
    val createdAt: Instant,
    val score: Score,
    val link: Uri,
    val postId: PostId,
    val controversialIndex: ControversialIndex,
    val depth: CommentDepth,
    val upVotes: UpVotesCount,
    val parentId: CommentId?,
)

@JvmInline
value class UpVotesCount(val value: Int)

@JvmInline
value class ControversialIndex(val value: Int)

@JvmInline
value class CommentDepth(val value: Int)

@JvmInline
value class CommentId(val value: String)

@JvmInline
value class CommentContent(val value: String)

enum class CommentFlags {
    SAVED,
    EDITED,
    IS_OP,
    STICKIED,
    SCORE_HIDDEN,
    LOCKED
}
