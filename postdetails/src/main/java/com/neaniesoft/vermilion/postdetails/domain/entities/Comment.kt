package com.neaniesoft.vermilion.postdetails.domain.entities

import android.net.Uri
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import org.commonmark.node.Node
import java.time.Instant

data class Comment(
    val id: CommentId,
    val content: CommentContent,
    val contentMarkdown: Node,
    val flags: Set<CommentFlags>,
    val authorName: AuthorName,
    val createdAt: Instant,
    val createdAtDurationString: DurationString,
    val score: Score,
    val link: Uri,
    val postId: PostId,
    val controversialIndex: ControversialIndex,
    val depth: CommentDepth,
    val upVotes: UpVotesCount,
    val parentId: CommentId?,
    val commentFlair: CommentFlair
)
