package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.NoThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.NsfwThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewSummary
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.SelfThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.SpoilerThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.Thumbnail
import com.neaniesoft.vermilion.posts.domain.entities.ThumbnailSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.UriThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.isNsfw
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import org.intellij.lang.annotations.Language
import java.time.Instant

@Composable
fun PostCard(
    post: Post,
    onClick: (Post) -> Unit,
    onMediaClicked: (Post) -> Unit,
    onCommunityClicked: (Community) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(elevation = 2.dp, modifier = modifier.clickable { onClick(post) }) {
        PostContent(
            post = post,
            modifier = modifier,
            shouldTruncate = true,
            shouldHideNsfw = true,
            onMediaClicked,
            onSummaryClicked = onClick,
            onCommunityClicked = onCommunityClicked
        )
    }
}

@Composable
fun PostContent(
    post: Post,
    modifier: Modifier = Modifier,
    shouldTruncate: Boolean,
    shouldHideNsfw: Boolean,
    onMediaClicked: (Post) -> Unit,
    onSummaryClicked: (Post) -> Unit,
    onCommunityClicked: (Community) -> Unit
) {
    // Log.d("PostContent", "Drawing content")
    Column(modifier = modifier.padding(0.dp)) {
        val summary = post.summary
        when (summary) {
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
                if (summary.preview != null) {
                    ImageSummary(
                        image = summary.preview,
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
            is TextPostSummary -> {
                // Do nothing, we draw the text post summary after the header
            }
        }
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = post.title.value,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 8.dp)
                        .weight(0.1f)
                )
                val hasPreview = when (post.summary) {
                    is PreviewSummary -> post.summary.preview != null
                    else -> false
                }

                val thumbnail = when (post.summary) {
                    is ThumbnailSummary -> post.summary.thumbnail
                    else -> NoThumbnail
                }

                if (!hasPreview && thumbnail !is NoThumbnail) {
                    Thumbnail(
                        thumbnail = thumbnail,
                        modifier = Modifier.size(72.dp)
                    ) { onMediaClicked(post) }
                }
            }
            if (summary is TextPostSummary) {
                TextSummary(content = summary.previewTextMarkdown, shouldTruncate) {
                    onSummaryClicked(post)
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (post.community is NamedCommunity) {
                        post.community.name.value
                    } else {
                        ""
                    },
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.clickable { onCommunityClicked(post.community) }
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
fun Thumbnail(thumbnail: Thumbnail, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Log.d("Thumbnail", "thumbnsil")
    val painter = when (thumbnail) {
        is SelfThumbnail, is DefaultThumbnail, is NoThumbnail, is NsfwThumbnail, is SpoilerThumbnail -> painterResource(
            id = R.drawable.ic_baseline_image_72
        )
        is UriThumbnail -> {
            rememberImagePainter(thumbnail.uri)
        }
    }
    Surface(
        shape = MaterialTheme.shapes.small, elevation = 4.dp,
        modifier = modifier
            .size(72.dp)
            .clickable { onClick() }
    ) {
        Image(
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Crop,
            painter = painter,
            contentDescription = "Thumbnail"
        )
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
        PostCard(post = DUMMY_TEXT_POST, {}, {}, {})
    }
}

@Preview(name = "Text Post card dark")
@Composable
fun PostCardPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostCard(post = DUMMY_TEXT_POST, {}, {}, {})
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

@Preview(name = "Thumbnail Post")
@Composable
fun ThumbnailPostPreview() {
    VermilionTheme(darkTheme = true) {
        PostCard(post = DUMMY_LINK_POST, onClick = {}, onMediaClicked = {}, {})
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
    "http://reddit.com/".toUri(),
    PostFlair.TextFlair(PostFlairText("Some flair"), PostFlairBackgroundColor(0))
)

internal val DUMMY_LINK_POST = Post(
    PostId(""),
    PostTitle("Some post with a very long title that is likely to split across multiple lines"),
    LinkPostSummary(
        null,
        DefaultThumbnail,
        LinkHost("somehost")
    ),
    NamedCommunity(CommunityName("Subreddit")),
    AuthorName("/u/SomeDude"),
    postedAt = Instant.now(),
    awardCounts = emptyMap(),
    CommentCount(123),
    Score(1024),
    flags = emptySet(),
    "http://reddit.com/".toUri(),
    PostFlair.TextFlair(PostFlairText("Some flair"), PostFlairBackgroundColor(0))
)
