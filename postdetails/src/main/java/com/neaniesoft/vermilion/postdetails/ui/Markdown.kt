package com.neaniesoft.vermilion.postdetails.ui

import androidx.compose.runtime.Composable
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
fun MarkdownBlockQuote(blockQuote: BlockQuote) {
}

@Composable
fun MarkdownThematicBreak(thematicBreak: ThematicBreak) {
}

@Composable
fun MarkdownHeading(heading: Heading) {
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
}

@Composable
fun MarkdownBulletList(bulletList: BulletList) {
}

@Composable
fun MarkdownOrderedList(orderedList: OrderedList) {
}
