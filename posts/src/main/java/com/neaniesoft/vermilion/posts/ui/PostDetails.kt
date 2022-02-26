package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant

@Composable
fun PostDetails(
    post: Post,
    modifier: Modifier = Modifier,
    onCommunityClicked: (Community) -> Unit = {}
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column() {
            // Subreddit
            val subredditName =
                if (post.community is NamedCommunity) {
                    buildAnnotatedString {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Light))
                        append("r/")
                        pop()
                        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
                        append(post.community.name.value)
                        pop()
                    }
                } else {
                    AnnotatedString("")
                }
            if (subredditName.isNotEmpty()) {
                Text(
                    style = MaterialTheme.typography.body2,
                    text = subredditName,
                    modifier = Modifier
                        .clickable { onCommunityClicked(post.community) }
                        .padding(bottom = 8.dp)
                )
            }

            // Comments
            val commentString = buildAnnotatedString {
                if (post.commentCount.value > 0) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append("${post.commentCount.value} ")
                    pop()
                    append(
                        stringResource(
                            id = if (post.commentCount.value == 1) {
                                R.string.post_details_comment
                            } else {
                                R.string.post_details_comments
                            }
                        )
                    )
                }
            }
            Text(text = commentString, style = MaterialTheme.typography.caption)
        }

        VoteSaveBlock(
            onUpVoteClicked = { { /*TODO*/ } },
            onDownVoteClicked = {},
            onSaveClicked = {}
        )

        val postedAtTime = remember {
            LocalPrettyTimeFormatter.format(post.postedAt)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = post.score.value.toString(),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = postedAtTime,
                style = MaterialTheme.typography.caption
            )
        }



    }
}

@Composable
fun VoteSaveBlock(
    onUpVoteClicked: () -> Unit,
    onDownVoteClicked: () -> Unit,
    onSaveClicked: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            IconButton(onClick = onDownVoteClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_downward_24),
                    contentDescription = "Down vote",
                    tint = MaterialTheme.colors.secondary
                )
            }
            IconButton(onClick = onSaveClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_star_24),
                    contentDescription = "Save"
                )
            }
        }

        Surface(
            elevation = 8.dp,
            shape = CircleShape,
            modifier = Modifier
                .size(64.dp)
        ) {
            IconButton(onClick = { onUpVoteClicked() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_arrow_upward_24),
                    contentDescription = "Up vote", tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Preview("Post Details Light")
@Composable
fun PostDetailsLightPreview() {
    VermilionTheme {
        androidx.compose.material.Surface {
            PostDetails(post = DUMMY_TEXT_POST, Modifier.fillMaxWidth())
        }
    }
}

@Preview("Post Details Dark")
@Composable
fun PostDetailsDarkPreview() {
    VermilionTheme(darkTheme = true) {
        androidx.compose.material.Surface {
            PostDetails(post = DUMMY_TEXT_POST, Modifier.fillMaxWidth())
        }
    }
}

interface DurationFormatter {
    fun format(instant: Instant): String
}

object LocalPrettyTimeFormatter : DurationFormatter {
    private val prettyTime = PrettyTime()

    override fun format(instant: Instant): String {
        return prettyTime.format(instant)
    }
}
