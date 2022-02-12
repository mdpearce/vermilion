package com.neaniesoft.vermilion.posts.adapters.driving.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.CommunityId
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import java.net.URL
import java.time.Instant

@Composable
fun PostCard(post: Post, modifier: Modifier = Modifier) {
    Card(elevation = 16.dp) {
        Column(modifier = modifier.padding(16.dp)) {
            when (val summary = post.summary) {
                is TextPostSummary -> {
                    TextSummary(content = summary.previewText.value)
                }
                is ImagePostSummary -> {
                    ImageSummary(imageUri = summary.previews.last().uri)
                }
                is LinkPostSummary -> TODO()
                is VideoPostSummary -> {
                    VideoSummary(previewImageUri = summary.previews.last().uri)
                }
            }
            Text(
                text = post.title.value,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = if (post.community is NamedCommunity) {
                        post.community.name.value
                    } else {
                        ""
                    },
                    style = MaterialTheme.typography.caption
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val commentString = when (val count = post.commentCount.value) {
                    0 -> stringResource(id = R.string.post_card_comment_count_0)
                    1 -> stringResource(id = R.string.post_card_comment_count_1)
                    else -> stringResource(id = R.string.post_card_comment_count_many, count)
                }
                Text(
                    text = commentString,
                    style = MaterialTheme.typography.caption
                )
                Text(text = post.score.value.toString(), style = MaterialTheme.typography.caption)
            }
        }
    }
}

@Composable
fun PostCardPlaceholder() {
    Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth().height(200.dp)) {
        
    }
}

@Composable
fun TextSummary(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun ImageSummary(imageUri: Uri) {
    val painter = rememberImagePainter(imageUri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Image(modifier = Modifier.size(maxWidth), painter = painter, contentDescription = "")
    }
}

@Composable
fun VideoSummary(previewImageUri: Uri) {
    val painter = rememberImagePainter(previewImageUri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Image(
            modifier = Modifier.size(maxWidth),
            painter = painter,
            contentDescription = ""
        )
        Image(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_play_circle_filled_24),
            contentDescription = "Video icon"
        )
    }
}

@Preview(name = "Text Post card light")
@Composable
fun PostCardPreview() {
    VermilionTheme {
        PostCard(post = DUMMY_TEXT_POST)
    }
}

@Preview(name = "Video summary")
@Composable
fun VideoSummaryPreview() {
    VermilionTheme {
        VideoSummary(previewImageUri = Uri.parse("https://www.google.com.au/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"))
    }
}

internal val DUMMY_TEXT_POST = Post(
    PostTitle("Some post with a very long title that is likely to split across multiple lines"),
    TextPostSummary(PreviewText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")),
    NamedCommunity(CommunityName("Subreddit"), CommunityId("")),
    AuthorName("/u/SomeDude"),
    postedAt = Instant.now(),
    awardCounts = emptyMap(),
    CommentCount(123),
    Score(1024),
    flags = emptySet(),
    URL("http://reddit.com/")
)
