package com.neaniesoft.vermilion.ui.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListBlock
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

// Very heavily inspired/taken from https://www.hellsoft.se/rendering-markdown-with-jetpack-compose/
@Composable
fun MarkdownDocument(
    document: Document,
    truncateToBlocks: Int = Int.MAX_VALUE,
    onUriClicked: (String) -> Unit = {},
    onTextClick: (() -> Unit)? = null
) {
    Column {
        MarkdownBlockChildren(parent = document, truncateToBlocks, onUriClicked, onTextClick)
    }
}

@Composable
fun MarkdownBlockChildren(
    parent: Node,
    truncateToBlocks: Int = Int.MAX_VALUE,
    onUriClicked: (String) -> Unit = {},
    onTextClick: (() -> Unit)? = null
) {
    var child = parent.firstChild
    var count = 0

    while (child != null && count < truncateToBlocks) {
        when (child) {
            is BlockQuote -> MarkdownBlockQuote(blockQuote = child)
            is ThematicBreak -> MarkdownThematicBreak(thematicBreak = child)
            is Heading -> MarkdownHeading(
                heading = child,
                onUriClicked = onUriClicked,
                onTextClick = onTextClick
            )
            is Paragraph -> MarkdownParagraph(
                paragraph = child,
                onClick = onTextClick,
                onUriClicked = onUriClicked
            )
            is FencedCodeBlock -> MarkdownFencedCodeBlock(fencedCodeBlock = child)
            is IndentedCodeBlock -> MarkdownIndentedCodeBlock(indentedCodeBlock = child)
            is Image -> MarkdownImage(image = child)
            is BulletList -> MarkdownBulletList(bulletList = child, onUriClicked = onUriClicked)
            is OrderedList -> MarkdownOrderedList(orderedList = child, onUriClicked = onUriClicked)
        }
        count++
        child = child.next
    }
}

@Composable
fun MarkdownBlockQuote(blockQuote: BlockQuote, modifier: Modifier = Modifier) {
    Box(Modifier.padding(bottom = 8.dp)) {
        Surface(
            elevation = 2.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                val text = buildAnnotatedString {
                    appendMarkdownChildren(blockQuote, MaterialTheme.colors)
                }
                Text(text, modifier.alpha(0.8f))
            }
        }
    }
}

@Composable
fun MarkdownThematicBreak(thematicBreak: ThematicBreak) {
    // Ignored
}

@Composable
fun MarkdownHeading(
    heading: Heading,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onTextClick: (() -> Unit)? = null
) {
    val style = when (heading.level) {
        1 -> MaterialTheme.typography.h1
        2 -> MaterialTheme.typography.h2
        3 -> MaterialTheme.typography.h3
        4 -> MaterialTheme.typography.h4
        5 -> MaterialTheme.typography.h5
        6 -> MaterialTheme.typography.h6
        else -> {
            // This isn't a header
            MarkdownBlockChildren(parent = heading)
            return
        }
    }

    val padding = if (heading.parent is Document) 8.dp else 0.dp
    Box(modifier.padding(bottom = padding)) {
        val text = buildAnnotatedString {
            appendMarkdownChildren(heading, MaterialTheme.colors)
        }
        MarkdownText(text, style, onUriClicked = onUriClicked, onClick = onTextClick)
    }
}

@Composable
fun MarkdownText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val links = text.getStringAnnotations(TAG_URL, 0, text.length)
    val clickableModifier = remember(text) {
        when {
            links.isNotEmpty() -> {
                Modifier.pointerInput(text) {
                    detectTapGestures { pos ->
                        layoutResult.value?.let { layoutResult ->
                            val position = layoutResult.getOffsetForPosition(pos)
                            val annotation = text.getStringAnnotations(position, position)
                                .firstOrNull()
                            if (annotation != null) {
                                if (annotation.tag == TAG_URL) {
                                    onUriClicked(annotation.item)
                                }
                            } else {
                                if (onClick != null) {
                                    onClick()
                                }
                            }
                        }
                    }
                }
            }
            onClick != null -> {
                Modifier.clickable { onClick() }
            }
            else -> {
                null
            }
        }
    }

    Text(
        text = text,
        modifier = if (clickableModifier != null) {
            modifier.then(clickableModifier)
        } else {
            modifier
        },
        style = style,
        inlineContent = mapOf(
            TAG_IMAGE_URL to InlineTextContent(
                Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Bottom)
            ) { url ->
                val painter = rememberImagePainter(url)
                Image(painter, "inline image")
            }
        ),
        onTextLayout = { layoutResult.value = it }
    )
}

private fun AnnotatedString.Builder.appendMarkdownChildren(parent: Node, colors: Colors) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Paragraph -> appendMarkdownChildren(child, colors)
            is Text -> append(child.literal)
            is Image -> appendInlineContent(TAG_IMAGE_URL, child.destination)
            is Emphasis -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is StrongEmphasis -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendMarkdownChildren(child, colors)
                pop()
            }
            is Code -> {
                pushStyle(TextStyle(fontFamily = FontFamily.Monospace).toSpanStyle())
                append(child.literal)
                pop()
            }
            is HardLineBreak -> {
                append("\n")
            }
            is Link -> {
                val underline = SpanStyle(colors.primary, textDecoration = TextDecoration.Underline)
                pushStyle(underline)
                pushStringAnnotation(TAG_URL, child.destination)
                appendMarkdownChildren(child, colors)
                pop()
                pop()
            }
        }

        child = child.next
    }
}

@Composable
fun MarkdownParagraph(
    paragraph: Paragraph,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    if (paragraph.firstChild is Image && paragraph.firstChild == paragraph.lastChild) {
        // Paragraph with single image
        MarkdownImage(image = paragraph.firstChild as Image, modifier)
    } else {
        val padding = if (paragraph.parent is Document) {
            8.dp
        } else {
            0.dp
        }
        Box(modifier.padding(bottom = padding)) {
            val styledText = buildAnnotatedString {
                pushStyle(MaterialTheme.typography.body1.toSpanStyle())
                appendMarkdownChildren(paragraph, MaterialTheme.colors)
                pop()
            }
            MarkdownText(
                text = styledText,
                style = MaterialTheme.typography.body1,
                onUriClicked = onUriClicked,
                onClick = onClick
            )
        }
    }
}

@Composable
fun MarkdownFencedCodeBlock(fencedCodeBlock: FencedCodeBlock, modifier: Modifier = Modifier) {
    val padding = if (fencedCodeBlock.parent is Document) {
        8.dp
    } else {
        0.dp
    }
    Box(modifier.padding(start = 8.dp, top = padding)) {
        Text(
            text = fencedCodeBlock.literal,
            style = TextStyle(fontFamily = FontFamily.Monospace),
            modifier = modifier
        )
    }
}

@Composable
fun MarkdownIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock, modifier: Modifier = Modifier) {
    val padding = if (indentedCodeBlock.parent is Document) {
        8.dp
    } else {
        0.dp
    }
    Box(modifier.padding(start = 8.dp, top = padding)) {
        Text(
            text = indentedCodeBlock.literal,
            style = TextStyle(fontFamily = FontFamily.Monospace),
            modifier = modifier
        )
    }
}

@Composable
fun MarkdownImage(image: Image, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val painter = rememberImagePainter(image.destination)
        Image(painter = painter, contentDescription = image.title)
    }
}

@Composable
fun MarkdownBulletList(
    bulletList: BulletList,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onTextClick: (() -> Unit)? = null
) {
    val marker = bulletList.bulletMarker
    MarkdownListItems(listBlock = bulletList, onUriClicked = onUriClicked, modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("$marker ")
            appendMarkdownChildren(it, MaterialTheme.colors)
            pop()
        }
        MarkdownText(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier,
            onUriClicked,
            onTextClick
        )
    }
}

@Composable
fun MarkdownListItems(
    listBlock: ListBlock,
    onUriClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    item: @Composable (node: Node) -> Unit
) {
    val bottom = if (listBlock.parent is Document) 8.dp else 0.dp
    val start = if (listBlock.parent is Document) 0.dp else 8.dp
    Column(modifier = modifier.padding(bottom = bottom, start = start)) {
        var listItem = listBlock.firstChild
        while (listItem != null) {
            var child = listItem.firstChild
            while (child != null) {
                when (child) {
                    is BulletList -> MarkdownBulletList(
                        child,
                        modifier,
                        onUriClicked = onUriClicked
                    )
                    is OrderedList -> MarkdownOrderedList(
                        child,
                        modifier,
                        onUriClicked = onUriClicked
                    )
                    else -> item(child)
                }
                child = child.next
            }
            listItem = listItem.next
        }
    }
}

@Composable
fun MarkdownOrderedList(
    orderedList: OrderedList,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onTextClick: (() -> Unit)? = null
) {
    var number = orderedList.startNumber
    val delimiter = orderedList.delimiter
    MarkdownListItems(orderedList, onUriClicked = onUriClicked, modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("${number++}$delimiter ")
            appendMarkdownChildren(it, MaterialTheme.colors)
            pop()
        }
        MarkdownText(text, MaterialTheme.typography.body1, modifier, onUriClicked, onTextClick)
    }
}

private const val TAG_URL = "url"
private const val TAG_IMAGE_URL = "imageUrl"

@Preview
@Composable
fun BlockQuotePreview() {
    VermilionTheme(darkTheme = true) {
        Surface(Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(8.dp)) {
                MarkdownDocument(document = DUMMY_BLOCK_QUOTE)
            }
        }
    }
}

private val parser = Parser.builder().build()

private val DUMMY_BLOCK_QUOTE = parser.parse(
    """
    > This is some text in a block quote. It is reasonably long so it will look like a paragraph when displayed
    
    I disagree wholeheartedly!
    """.trimIndent()
) as Document
