package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.isNsfw
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import org.intellij.lang.annotations.Language
import java.net.URL
import java.time.Instant

@Composable
fun PostCard(
    post: Post,
    onClick: (Post) -> Unit,
    onMediaClicked: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(elevation = 2.dp, modifier = modifier.clickable { onClick(post) }) {
        PostSummary(
            post = post,
            modifier = modifier,
            shouldTruncate = true,
            shouldHideNsfw = true,
            onMediaClicked,
            onSummaryClicked = onClick
        )
    }
}

@Composable
fun PostSummary(
    post: Post,
    modifier: Modifier = Modifier,
    shouldTruncate: Boolean,
    shouldHideNsfw: Boolean,
    onMediaClicked: (Post) -> Unit,
    onSummaryClicked: (Post) -> Unit
) {
    Column(modifier = modifier.padding(16.dp)) {
        when (val summary = post.summary) {
            is TextPostSummary -> {
                TextSummary(content = summary.previewTextMarkdown, shouldTruncate) {
                    onSummaryClicked(post)
                }
            }
            is ImagePostSummary -> {
                ImageSummary(
                    image = summary.preview ?: UriImage("".toUri(), 0, 0),
                    isNsfw = if (shouldHideNsfw) {
                        post.isNsfw()
                    } else {
                        false
                    },
                    shouldTruncate = shouldTruncate
                ) { onMediaClicked(post) }
            }
            is LinkPostSummary -> {
                ImageSummary(
                    image = summary.preview ?: UriImage("".toUri(), 0, 0),
                    shouldTruncate = shouldTruncate,
                    isNsfw = if (shouldHideNsfw) {
                        post.isNsfw()
                    } else {
                        false
                    }
                ) {
                    onMediaClicked(post)
                }
            }
            is VideoPostSummary -> {
                VideoSummary(
                    image = summary.preview ?: UriImage("".toUri(), 0, 0),
                    isNsfw = if (shouldHideNsfw) {
                        post.isNsfw()
                    } else {
                        false
                    },
                    shouldTruncate = shouldTruncate
                ) { onMediaClicked(post) }
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

@Composable
fun PostCardPlaceholder() {
    Card(
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
    }
}

@Preview(name = "Text Post card light")
@Composable
fun PostCardPreview() {
    VermilionTheme {
        PostCard(post = DUMMY_TEXT_POST, {}, {})
    }
}

@Preview(name = "Text Post summary")
@Composable
fun TextPostSummaryPreview() {
    VermilionTheme {
        TextSummary(
            content = Parser.builder().build().parse(MIXED_MD) as Document,
            shouldTruncate = false
        )
    }
}

@Language("Markdown")
const val MIXED_MD = """
### Markdown Header
This is regular text without formatting in a single paragraph.
![Serious](file:///android_asset/serios.jpg)
Images can also be inline: ![Serious](file:///android_asset/serios.jpg). [Links](http://hellsoft.se) and `inline code` also work. This *is* text __with__ inline styles for *__bold and italic__*. Those can be nested.
Here is a code block:
```javascript
function codeBlock() {
    return true;
}
```

Here is another clode block (indented):

    fun foo() {
        bar()
    }

+ Bullet
+ __Lists__
+ Are
+ *Cool*
1. **First**
1. *Second*
1. Third
1. [Fourth is clickable](https://google.com)  
   1. And
   1. Sublists
1. Mixed
   - With
   - Bullet
   - Lists
100) Lists
100) Can
100) Have
100) *Custom*
100) __Start__
100) Numbers
- List
- Of
- Items
  - With
  - Sublist
> A blockquote is useful for quotes!
"""

private const val DUMMY_CONTENT =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
internal val DUMMY_TEXT_POST = Post(
    PostId(""),
    PostTitle("Some post with a very long title that is likely to split across multiple lines"),
    TextPostSummary(
        PreviewText(DUMMY_CONTENT),
        Parser.builder().build().parse(MIXED_MD) as Document
    ),
    NamedCommunity(CommunityName("Subreddit")),
    AuthorName("/u/SomeDude"),
    postedAt = Instant.now(),
    awardCounts = emptyMap(),
    CommentCount(123),
    Score(1024),
    flags = emptySet(),
    URL("http://reddit.com/")
)
