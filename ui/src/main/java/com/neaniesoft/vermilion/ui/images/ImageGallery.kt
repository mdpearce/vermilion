package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.neaniesoft.vermilion.ui.dialogs.FullscreenDialog

@ExperimentalPagerApi
@Composable
fun ImageGallery(
    images: List<Uri>,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState()
) {
    HorizontalPager(count = images.size, state = pagerState, modifier = modifier) {
        val painter = rememberImagePainter(images[currentPage])
        Image(
            painter = painter,
            contentDescription = "Image ${currentPage + 1}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun ImageGalleryDialog(
    images: List<Uri>,
    onDismiss: () -> Unit
) {
    if (images.isNotEmpty()) {
        FullscreenDialog(onDismiss = onDismiss) {
            val state = rememberPagerState()
            ImageGallery(images = images, modifier = Modifier.fillMaxSize(), pagerState = state)
        }
    }
}
