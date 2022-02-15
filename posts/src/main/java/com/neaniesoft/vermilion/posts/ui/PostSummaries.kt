package com.neaniesoft.vermilion.posts.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.max
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.domain.entities.UriImage

@Composable
fun TextSummary(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun ImageSummary(image: UriImage) {
    val painter = rememberImagePainter(image.uri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val ratio = image.width.toFloat() / image.height.toFloat()
        val actualImageWidth = with(LocalDensity.current) {
            image.width.toDp()
        }
        val imageWidth = max(actualImageWidth, maxWidth)
        Image(
            modifier = Modifier.size(imageWidth, imageWidth.div(ratio)),
            painter = painter,
            contentDescription = ""
        )
    }
}

@Composable
fun VideoSummary(image: UriImage) {
    val painter = rememberImagePainter(image.uri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val ratio = image.width.toFloat() / image.height.toFloat()
        val actualImageWidth = with(LocalDensity.current) {
            image.width.toDp()
        }
        val imageWidth = max(actualImageWidth, maxWidth)
        Image(
            modifier = Modifier.size(imageWidth, imageWidth.div(ratio)),
            painter = painter,
            contentDescription = ""
        )
    }
}
