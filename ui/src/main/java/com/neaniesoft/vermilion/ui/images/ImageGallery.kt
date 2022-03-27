package com.neaniesoft.vermilion.ui.images

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var isBottomBarVisible: Boolean by remember {
        mutableStateOf(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                isBottomBarVisible = !isBottomBarVisible
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val painter = rememberImagePainter(images[page].uri)
            val zoomableState = rememberZoomableState(maxScale = 6f)
            Zoomable(
                state = zoomableState,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Image ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        AnimatedVisibility(visible = isBottomBarVisible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .background(AlmostBlack.copy(alpha = 0.5f))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
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
