package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.ui.markdown.MarkdownDocument
import org.commonmark.node.Document

@Composable
fun TextSummary(content: Document, shouldTruncate: Boolean, onClick: (() -> Unit)? = null) {
    Column((if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(16.dp)) {
        MarkdownDocument(document = content, if (shouldTruncate) 3 else Int.MAX_VALUE, onClick)
    }
}

private const val MIN_RATIO = 1.0f

@Composable
fun ImageSummary(image: UriImage, shouldTruncate: Boolean, isNsfw: Boolean, onClick: () -> Unit) {
    if (isNsfw) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            contentScale = ContentScale.FillWidth,
            painter = painterResource(id = R.drawable.image_hidden),
            contentDescription = stringResource(
                id = R.string.nsfw_content_description
            )
        )
    } else {
        val painter = rememberImagePainter(image.uri.toString()) {
            placeholder(R.drawable.image_placeholder)
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val ratio = if (shouldTruncate) {
                kotlin.math.max((image.width.toFloat() / image.height.toFloat()), MIN_RATIO)
            } else {
                image.width.toFloat() / image.height.toFloat()
            }
            Image(
                modifier = Modifier
                    .size(maxWidth, maxWidth.div(ratio))
                    .clickable { onClick() },
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun VideoSummary(image: UriImage, shouldTruncate: Boolean, isNsfw: Boolean, onClick: () -> Unit) {
    ImageSummary(image = image, shouldTruncate, isNsfw, onClick)
}
