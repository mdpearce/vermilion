package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.BlurTransformation
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.ui.markdown.MarkdownDocument
import org.commonmark.node.Document

@Composable
fun TextSummary(
    content: Document,
    shouldTruncate: Boolean,
    modifier: Modifier = Modifier,
    onUriClicked: (String) -> Unit = {},
    onClick: (() -> Unit)? = null
) {

    Column((if (onClick != null) modifier.clickable { onClick() } else modifier)) {
        Divider(Modifier.padding(bottom = 8.dp, top = 8.dp))
        MarkdownDocument(
            document = content,
            if (shouldTruncate) 3 else Int.MAX_VALUE,
            onUriClicked,
            onClick
        )
    }
}

@Composable
fun ImageSummary(image: UriImage, isNsfw: Boolean, onClick: () -> Unit) {
    val painter = rememberImagePainter(data = image.uri.toString()) {
        placeholder(R.drawable.image_placeholder)
        if (isNsfw) {
            transformations(BlurTransformation(LocalContext.current, 25f, 8f))
        }
        crossfade(200)
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val ratio = remember {
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

@Composable
fun VideoSummary(image: UriImage, isNsfw: Boolean, onClick: () -> Unit) {
    ImageSummary(image = image, isNsfw, onClick)
}
