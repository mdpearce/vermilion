package com.neaniesoft.vermilion.postdetails.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.neaniesoft.vermilion.postdetails.R
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.ui.markdown.MarkdownDocument
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import org.commonmark.node.Document
import java.time.Instant

@Composable
fun CommentRow(comment: Comment, modifier: Modifier = Modifier) {
    Row(
        modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {

        repeat(comment.depth.value) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(16.dp)
                    .padding(start = 7.dp, end = 7.dp)
                    .background(MaterialTheme.colors.onBackground)
            )
        }

        Column(Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = comment.authorName.value,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = comment.score.value.toString(),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1.0f))
                Text(
                    text = comment.createdAtDurationString.value,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onBackground,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Box(Modifier.padding(top = 8.dp)) {
                MarkdownDocument(document = comment.contentMarkdown as Document)
            }
        }
    }
}

@Composable
fun MoreCommentsStubRow(stub: CommentStub, modifier: Modifier = Modifier) {
    Row(
        modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        repeat(stub.depth.value) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(16.dp)
                    .padding(start = 7.dp, end = 7.dp)
                    .background(MaterialTheme.colors.onBackground)
            )
        }
        Text(
            text = stringResource(id = R.string.more_comments, stub.count.value),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .alignByBaseline()
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
fun CommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        CommentRow(DUMMY_COMMENT, Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
fun DeepCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        CommentRow(DEEP_DUMMY_COMMENT, Modifier.fillMaxWidth())
    }
}

private val DUMMY_COMMENT = Comment(
    CommentId("id"),
    CommentContent("This is a pretty long comment that might split over several lines. It's got several sentences and goes on for some time. Still going here."),
    Document(),
    emptySet(),
    AuthorName("Some user"),
    Instant.now(),
    DurationString("1h ago"),
    Score(100),
    "".toUri(),
    PostId("post_id"),
    ControversialIndex(0),
    CommentDepth(0),
    UpVotesCount(88),
    null
)

private val DEEP_DUMMY_COMMENT = DUMMY_COMMENT.copy(depth = CommentDepth(6))
