package com.neaniesoft.vermilion.postdetails.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Document
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.ThematicBreak

// Very heavily inspired/taken from https://www.hellsoft.se/rendering-markdown-with-jetpack-compose/
@Composable
fun MarkdownDocument(document: Document) {
}

@Composable
fun MarkdownBlockChildren(parent: Node) {
    var child = parent.firstChild

    while (child != null) {
        when (child) {
            is BlockQuote -> MarkdownBlockQuote(blockQuote = child)
            is ThematicBreak -> MarkdownThematicBreak(thematicBreak = child)
            is Heading -> MarkdownHeading(heading = child)
            is Paragraph -> MarkdownParagraph(paragraph = child)
            is FencedCodeBlock -> MarkdownFencedCodeBlock(fencedCodeBlock = child)
            is IndentedCodeBlock -> MarkdownIndentedCodeBlock(indentedCodeBlock = child)
            is Image -> MarkdownImage(image = child)
            is BulletList -> MarkdownBulletList(bulletList = child)
            is OrderedList -> MarkdownOrderedList(orderedList = child)
        }
        child = child.next
    }
}

@Composable
fun MarkdownBlockQuote(blockQuote: BlockQuote, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colors.onBackground
    Box(modifier = Modifier
        .drawBehind {
            drawLine(
                color = color,
                strokeWidth = 2f,
                start = Offset(12.dp.value, 0f),
                end = Offset(12.dp.value, size.height)
            )
        }
        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
        val text = buildAnnotatedString {
            pushStyle(
                MaterialTheme.typography.body1.toSpanStyle()
                    .plus(SpanStyle(fontStyle = FontStyle.Italic))
            )
            appendMarkdownChildren(blockQuote, MaterialTheme.colors)
            pop()
        }
        Text(text, modifier)
    }
}

@Composable
fun MarkdownThematicBreak(thematicBreak: ThematicBreak) {
}

@Composable
fun MarkdownHeading(heading: Heading, modifier: Modifier = Modifier) {
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
        MarkdownText(text, style)
    }
}

@Composable
fun MarkdownText(text: AnnotatedString, style: TextStyle) {
    TODO("Not yet implemented")
}

private fun AnnotatedString.Builder.appendMarkdownChildren(node: Node, colors: Colors) {
    TODO("Not yet implemented")
}

@Composable
fun MarkdownParagraph(paragraph: Paragraph) {
}

@Composable
fun MarkdownFencedCodeBlock(fencedCodeBlock: FencedCodeBlock) {
}

@Composable
fun MarkdownIndentedCodeBlock(indentedCodeBlock: IndentedCodeBlock) {
}

@Composable
fun MarkdownImage(image: Image) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val painter = rememberImagePainter(image.destination)
        Image(painter = painter, contentDescription = image.title)
    }
}

@Composable
fun MarkdownBulletList(bulletList: BulletList) {
}

@Composable
fun MarkdownOrderedList(orderedList: OrderedList) {
}
