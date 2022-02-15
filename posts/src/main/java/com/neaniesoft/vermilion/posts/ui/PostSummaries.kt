package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.UriImage

@Composable
fun TextSummary(content: String, shouldTruncate: Boolean) {
    Text(
        text = content,
        style = MaterialTheme.typography.body1,
        maxLines = if (shouldTruncate) {
            8
        } else {
            Int.MAX_VALUE
        },
        overflow = TextOverflow.Ellipsis
    )
}

private const val MIN_RATIO = 1.0f

@Composable
fun ImageSummary(image: UriImage, shouldTruncate: Boolean, onClick: () -> Unit) {
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

@Composable
fun VideoSummary(image: UriImage, shouldTruncate: Boolean, onClick: () -> Unit) {
    ImageSummary(image = image, shouldTruncate, onClick)
}
