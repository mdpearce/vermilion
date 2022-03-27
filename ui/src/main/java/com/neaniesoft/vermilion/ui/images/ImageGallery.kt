package com.neaniesoft.vermilion.ui.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.neaniesoft.vermilion.coreentities.UriImage
import com.neaniesoft.vermilion.ui.dialogs.FullscreenDialog
import com.neaniesoft.vermilion.ui.theme.AlmostBlack

@ExperimentalPagerApi
@Composable
fun ImageGallery(
    images: List<UriImage>,
    pagerState: PagerState = rememberPagerState()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val painter = rememberImagePainter(images[page].uri)
            Image(
                painter = painter,
                contentDescription = "Image ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Box(
            modifier = Modifier
                .background(AlmostBlack.copy(alpha = 0.5f))
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState, modifier = Modifier
                    .padding(16.dp)
            )
        }
    }
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun ImageGalleryDialog(
    images: List<UriImage>,
    onDismiss: () -> Unit
) {
    if (images.isNotEmpty()) {
        FullscreenDialog(onDismiss = onDismiss) {
            val state = rememberPagerState()
            ImageGallery(images = images, pagerState = state)
        }
    }
}
