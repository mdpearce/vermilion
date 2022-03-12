package com.neaniesoft.vermilion.posts.domain.entities

import org.commonmark.node.Document

data class MarkdownText(
    val raw: String,
    val markdown: Document
)
