package com.neaniesoft.vermilion.posts.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.posts.R
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@Composable
fun TextSummary(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun ImageSummary(imageUri: Uri) {
    val painter = rememberImagePainter(imageUri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Image(modifier = Modifier.size(maxWidth), painter = painter, contentDescription = "")
    }
}

@Composable
fun VideoSummary(previewImageUri: Uri) {
    val painter = rememberImagePainter(previewImageUri.toString())

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Image(
            modifier = Modifier.size(maxWidth),
            painter = painter,
            contentDescription = ""
        )
        Image(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_baseline_play_circle_filled_24),
            contentDescription = "Video icon"
        )
    }
}

@Preview(name = "Video summary")
@Composable
fun VideoSummaryPreview() {
    VermilionTheme {
        VideoSummary(previewImageUri = Uri.parse("https://www.google.com.au/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"))
    }
}
