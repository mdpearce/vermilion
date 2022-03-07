package com.neaniesoft.vermilion.postdetails.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.neaniesoft.vermilion.postdetails.R
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentContent
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlags
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlair
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairBackgroundColor
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairText
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentFlairTextColor
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.postdetails.domain.entities.ControversialIndex
import com.neaniesoft.vermilion.postdetails.domain.entities.DurationString
import com.neaniesoft.vermilion.postdetails.domain.entities.UpVotesCount
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.ui.markdown.MarkdownDocument
import com.neaniesoft.vermilion.ui.theme.AlmostBlack
import com.neaniesoft.vermilion.ui.theme.Green400
import com.neaniesoft.vermilion.ui.theme.LightRedVariant
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import com.neaniesoft.vermilion.ui.theme.colorForDepth
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.text.NumberFormat
import java.time.Instant

@Composable
fun CommentRow(
    comment: Comment,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {}
) {
    Column {
        if (comment.depth == CommentDepth(0)) {
            Divider()
        }
        Row(
            modifier.height(intrinsicSize = IntrinsicSize.Min)
        ) {

            DepthIndicators(depth = comment.depth.value)

            Column(Modifier.padding(8.dp)) {
                CommentTime(comment = comment, Modifier.padding(bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CommentAuthor(comment = comment, Modifier.padding(end = 8.dp))
                    CommentTime(comment = comment, Modifier.padding(end = 8.dp))
                    CommentFlair(flair = comment.commentFlair, Modifier.padding(end = 8.dp))
                    CommentFlagIcons(flags = comment.flags)
                    Spacer(modifier = Modifier.weight(1.0f))
                    CommentScore(comment = comment)
                }

                Box(Modifier.padding(top = 8.dp)) {
                    MarkdownDocument(
                        document = comment.contentMarkdown as Document,
                        onUriClicked = onUriClicked
                    )
                }
            }
        }
    }
}

@Composable
fun CommentTime(comment: Comment, modifier: Modifier = Modifier) {
    val time = buildAnnotatedString {
        append(comment.createdAtDurationString.value)
        if (comment.editedAtDurationString != null) {
            append(" ")
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            append("(edited ${comment.editedAtDurationString.value})") // TODO use a string resource
            pop()
        }
    }
    Text(
        text = time,
        style = MaterialTheme.typography.caption,
        modifier = modifier
    )
}

@Composable
fun CommentAuthor(comment: Comment, modifier: Modifier = Modifier) {
    if (comment.flags.contains(CommentFlags.IS_OP)) {
        Text(
            text = comment.authorName.value,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = modifier
        )
    } else {
        Text(
            text = comment.authorName.value,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.secondary,
            modifier = modifier
        )
    }
}

@Composable
fun CommentScore(comment: Comment, modifier: Modifier = Modifier) {
    val score = remember {
        if (comment.flags.contains(CommentFlags.SCORE_HIDDEN)) {
            "?"
        } else {
            NumberFormat.getIntegerInstance().format(comment.score.value)
        }
    }

    Text(
        text = score,
        style = MaterialTheme.typography.caption,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun CommentFlagIcons(flags: Set<CommentFlags>) {
    Row {
        flags.forEach { flag ->
            val icon = when (flag) {
                CommentFlags.STICKIED -> {
                    FlagIcon(
                        drawable = R.drawable.ic_baseline_push_pin_24,
                        contentDescription = "Sticky",
                        tint = Green400
                    )
                }
                CommentFlags.IS_MOD -> {
                    FlagIcon(
                        drawable = R.drawable.ic_baseline_local_police_24,
                        contentDescription = "Moderator",
                        tint = Green400
                    )
                }
                CommentFlags.IS_ADMIN -> {
                    FlagIcon(
                        drawable = R.drawable.ic_baseline_verified_user_24,
                        contentDescription = "Admin",
                        tint = LightRedVariant
                    )
                }
                else -> {
                    null
                }
            }
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon.drawable),
                    contentDescription = icon.contentDescription,
                    tint = icon.tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }
}

data class FlagIcon(
    @DrawableRes val drawable: Int,
    val contentDescription: String,
    val tint: Color
)

@Composable
fun DepthIndicators(
    depth: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(start = (depth * 8).dp, top = 4.dp, bottom = 4.dp)) {
        if (depth != 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(colorForDepth(depth))
            )
        }
    }
}

@Composable
fun MoreCommentsStubRow(
    stub: CommentStub,
    modifier: Modifier = Modifier,
    onClick: (CommentStub) -> Unit
) {
    Row(
        modifier
            .height(intrinsicSize = IntrinsicSize.Min)
            .clickable { onClick(stub) }
    ) {
        DepthIndicators(depth = stub.depth.value)
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

@Composable
fun CommentFlair(flair: CommentFlair, modifier: Modifier = Modifier) {
    if (flair is CommentFlair.TextFlair) {
        val flairBackgroundColor =
            if (flair.backgroundColor == CommentFlairBackgroundColor(0)) {
                MaterialTheme.colors.surface
            } else {
                Color(flair.backgroundColor.value)
            }
        val flairTextColor =
            if (flair.backgroundColor == CommentFlairBackgroundColor(0)) {
                MaterialTheme.colors.onSurface
            } else {
                when (flair.textColor) {
                    CommentFlairTextColor.DARK -> AlmostBlack
                    CommentFlairTextColor.LIGHT -> Color.White
                }
            }
        Surface(
            color = flairBackgroundColor,
            contentColor = flairTextColor,
            shape = MaterialTheme.shapes.small,
            elevation = 2.dp,
            modifier = modifier
        ) {
            Text(
                text = flair.text.value,
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Preview
@Composable
fun CommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(DUMMY_COMMENT, Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
fun DeepCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(DEEP_DUMMY_COMMENT, Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
fun StickiedCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(STICKIED_DUMMY_COMMENT)
        }
    }
}

@Preview
@Composable
fun StickiedModCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(STICKED_MOD_DUMMY_COMMENT)
        }
    }
}

@Preview
@Composable
fun AdminCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(ADMIN_DUMMY_COMMENT)
        }
    }
}

@Preview
@Composable
fun OpCommentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(OP_DUMMY_CONTENT)
        }
    }
}

@Preview
@Composable
fun EditedCOmmentRowPreview() {
    VermilionTheme(darkTheme = true) {
        Surface {
            CommentRow(EDITED_DUMMY_COMMENT)
        }
    }
}

private val DUMMY_COMMENT = Comment(
    CommentId("id"),
    CommentContent("This is a pretty long comment that might split over several lines. It's got several sentences and goes on for some time. Still going here."),
    Parser.builder().build()
        .parse("This is a pretty long comment that might split over several lines. It's got several sentences and goes on for some time. Still going here."),
    emptySet(),
    AuthorName("Some user"),
    Instant.now(),
    DurationString("23m"),
    null,
    null,
    Score(1024),
    "".toUri(),
    PostId("post_id"),
    ControversialIndex(0),
    CommentDepth(0),
    UpVotesCount(88),
    null,
    CommentFlair.TextFlair(
        CommentFlairText("Some flair"),
        CommentFlairBackgroundColor(0),
        CommentFlairTextColor.DARK
    )
)

private val DEEP_DUMMY_COMMENT = DUMMY_COMMENT.copy(depth = CommentDepth(6))

private val STICKIED_DUMMY_COMMENT = DUMMY_COMMENT.copy(flags = setOf(CommentFlags.STICKIED))
private val STICKED_MOD_DUMMY_COMMENT =
    DUMMY_COMMENT.copy(flags = setOf(CommentFlags.STICKIED, CommentFlags.IS_MOD))
private val ADMIN_DUMMY_COMMENT = DUMMY_COMMENT.copy(flags = setOf(CommentFlags.IS_ADMIN))
private val OP_DUMMY_CONTENT = DUMMY_COMMENT.copy(flags = setOf(CommentFlags.IS_OP))
private val EDITED_DUMMY_COMMENT = DUMMY_COMMENT.copy(
    flags = setOf(CommentFlags.EDITED),
    editedAt = Instant.now(),
    editedAtDurationString = DurationString("30m")
)
