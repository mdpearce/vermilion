package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.posts.domain.entities.*
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
                is ImagePostSummary -> TODO()
                is LinkPostSummary -> TODO()
            }
            Text(
                text = post.title.value,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(text = post.communityName.value, style = MaterialTheme.typography.caption)
            }
        }
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
fun ImageSummary(imageUrl: URL) {
}

@Preview(name = "Text Post card light")
@Composable
fun PostCardPreview() {
    VermilionTheme {
        PostCard(post = DUMMY_TEXT_POST)
    }
}

internal val DUMMY_TEXT_POST = Post(
    PostTitle("Some post with a very long title that is likely to split across multiple lines"),
    TextPostSummary(PreviewText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")),
    CommunityName("Subreddit"),
    URL("https://some.url/someicon.png"),
    AuthorName("/u/SomeDude"),
    postedAt = Instant.now(),
    CommentCount(123),
    Score(1024),
    URL("http://reddit.com/")
)