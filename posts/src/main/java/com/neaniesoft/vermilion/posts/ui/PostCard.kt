package com.neaniesoft.vermilion.posts.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.MarkdownText
import com.neaniesoft.vermilion.posts.domain.entities.NoThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.NsfwThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairTextColor
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.SelfThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.SpoilerThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.Thumbnail
import com.neaniesoft.vermilion.posts.domain.entities.UriThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.isNsfw
import com.neaniesoft.vermilion.ui.theme.AlmostBlack
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
    onUriClicked: (Uri) -> Unit,
    onUpVoteClicked: (Post) -> Unit,
    onDownVoteClicked: (Post) -> Unit,
    modifier: Modifier = Modifier,
    shouldHideNsfw: Boolean = false
) {
    Card(elevation = 2.dp, modifier = modifier.clickable { onClick(post) }) {
        PostContent(
            post = post,
            modifier = modifier,
            shouldTruncate = true,
            shouldHideNsfw = shouldHideNsfw,
            onMediaClicked,
            onSummaryClicked = onClick,
            onCommunityClicked = onCommunityClicked,
            onUriClicked = onUriClicked,
            onUpVoteClicked = onUpVoteClicked,
            onDownVoteClicked = onDownVoteClicked
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
    onCommunityClicked: (Community) -> Unit,
    onUriClicked: (Uri) -> Unit,
    onUpVoteClicked: (Post) -> Unit,
    onDownVoteClicked: (Post) -> Unit
) {
    Column(modifier = modifier.padding(0.dp)) {

        if (post.imagePreview != null) {
            Box(contentAlignment = Alignment.BottomStart) {
                ImageSummary(
                    image = post.imagePreview,
                    isNsfw = if (shouldHideNsfw) {
                        post.isNsfw()
                    } else {
                        false
                    }
                ) {
                    onMediaClicked(post)
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    PostTypeIndicator(type = post.type)
                }
            }
        }
        val contentAlpha = remember {
            if (post.flags.contains(PostFlags.VIEWED)) {
                0.5f
            } else {
                1.0f
            }
        }
        val hasPreview = post.imagePreview != null

        Column(Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
            if (post.type != Post.Type.TEXT) {
                LinkHost(host = post.linkHost)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 8.dp)
            ) {
                Text(
                    text = post.title.value,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 8.dp)
                        .weight(0.1f)
                        .alpha(contentAlpha)
                )

                val thumbnail = post.thumbnail

                if (!hasPreview && thumbnail !is NoThumbnail) {
                    Thumbnail(
                        thumbnail = thumbnail,
                        modifier = Modifier.size(72.dp)
                    ) { onMediaClicked(post) }
                }
            }

            PostFlair(flair = post.flair, Modifier.padding(bottom = 4.dp))

            if (post.text != null) {
                TextSummary(
                    content = post.text.markdown,
                    shouldTruncate,
                    modifier = Modifier.alpha(contentAlpha),
                    onUriClicked = { onUriClicked(it.toUri()) }
                ) {
                    onSummaryClicked(post)
                }
            }

            Divider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

            PostDetails(
                post = post,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onCommunityClicked = onCommunityClicked,
                onUpVoteClicked = onUpVoteClicked,
                onDownVoteClicked = onDownVoteClicked,
                onSaveClicked = {}
            )
        }
    }
}

@Composable
fun Chip(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = MaterialTheme.colors.onSurface,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small.copy(CornerSize(50)),
        elevation = elevation,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)) {
            content()
        }
    }
}

@Composable
fun PostFlair(flair: PostFlair, modifier: Modifier = Modifier) {
    if (flair is PostFlair.TextFlair) {
        val flairBackgroundColor =
            if (flair.backgroundColor == PostFlairBackgroundColor(0)) {
                MaterialTheme.colors.surface
            } else {
                Color(flair.backgroundColor.value)
            }
        val flairTextColor =
            if (flair.backgroundColor == PostFlairBackgroundColor(0)) {
                MaterialTheme.colors.onSurface
            } else {
                when (flair.textColor) {
                    PostFlairTextColor.DARK -> AlmostBlack
                    PostFlairTextColor.LIGHT -> Color.White
                }
            }
        Chip(
            color = flairBackgroundColor,
            contentColor = flairTextColor,
            elevation = 4.dp,
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

@Composable
fun LinkHost(host: LinkHost, modifier: Modifier = Modifier) {
    if (!host.value.startsWith("self.")) {
        Text(
            text = host.value,
            style = MaterialTheme.typography.caption,
            modifier = modifier
        )
    }
}

@Composable
fun PostTypeIndicator(type: Post.Type, modifier: Modifier = Modifier) {
    val resource = when (type) {
        Post.Type.LINK -> R.drawable.ic_baseline_link_24
        Post.Type.VIDEO -> R.drawable.ic_baseline_ondemand_video_24
        else -> 0
    }
    if (resource != 0) {
        val painter = painterResource(
            id = resource
        )
        Surface(
            shape = MaterialTheme.shapes.small.copy(all = CornerSize(50)),
            modifier = modifier,
            elevation = 4.dp
        ) {
            Icon(
                painter = painter,
                contentDescription = type.name,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun Thumbnail(thumbnail: Thumbnail, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Log.d("Thumbnail", "thumbnail: $thumbnail (${thumbnail.identifier})")
    val painter = when (thumbnail) {
        is SelfThumbnail, is DefaultThumbnail, is NsfwThumbnail, is SpoilerThumbnail -> painterResource(
            id = R.drawable.ic_baseline_image_72
        )
        is UriThumbnail -> {
            rememberImagePainter(thumbnail.uri)
        }
        is NoThumbnail -> {
            null
        }
    }
    if (painter != null) {
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
        PostCard(post = DUMMY_TEXT_POST, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Text Post card dark")
@Composable
fun PostCardPreviewDark() {
    VermilionTheme(darkTheme = true) {
        PostCard(post = DUMMY_TEXT_POST, {}, {}, {}, {}, {}, {})
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
        PostCard(post = DUMMY_LINK_POST, onClick = {}, onMediaClicked = {}, {}, {}, {}, {})
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
val DUMMY_TEXT_POST = Post(
    PostId(""),
    PostTitle("Some post with a very long title that is likely to split across multiple lines"),
    null,
    null,
    NoThumbnail,
    LinkHost("some.host"),
    MarkdownText(DUMMY_CONTENT, Parser.builder().build().parse(DUMMY_CONTENT) as Document),
    null,
    null,
    NamedCommunity(CommunityName("Subreddit"), CommunityId("")),
    AuthorName("/u/SomeDude"),
    postedAt = Instant.now(),
    awardCounts = emptyMap(),
    CommentCount(123),
    Score(1024),
    flags = emptySet(),
    "http://reddit.com/".toUri(),
    PostFlair.TextFlair(
        PostFlairText("Some flair"),
        PostFlairBackgroundColor(0),
        PostFlairTextColor.DARK
    ),
    Post.Type.TEXT
)

val DUMMY_LINK_POST = DUMMY_TEXT_POST.copy(
    text = null,
    type = Post.Type.LINK
)
