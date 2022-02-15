package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.posts.domain.entities.UriImage

@Composable
fun TextSummary(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.body1
    )
}

private const val MIN_RATIO = 1.0f

@Composable
fun ImageSummary(image: UriImage) {
    val painter = rememberImagePainter(image.uri.toString()) {
        placeholder(R.drawable.image_placeholder)
        crossfade(true)

    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val ratio = kotlin.math.max((image.width.toFloat() / image.height.toFloat()), MIN_RATIO)
        Image(
            modifier = Modifier.size(maxWidth, maxWidth.div(ratio)),
            painter = painter,
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun VideoSummary(image: UriImage) {
    ImageSummary(image = image)
}
