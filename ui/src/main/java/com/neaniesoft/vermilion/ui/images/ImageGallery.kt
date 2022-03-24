package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.neaniesoft.vermilion.ui.dialogs.FullscreenDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

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
    postId: String,
    onDismiss: () -> Unit,
    viewModel: ImageGalleryViewModel = hiltViewModel()
) {

    val images by viewModel.images.collectAsState()

    LaunchedEffect(key1 = postId) {
        viewModel.onPostId(postId)
    }

    if (images.isNotEmpty()) {
        FullscreenDialog(onDismiss = onDismiss) {
            val state = rememberPagerState()
            ImageGallery(images = images, modifier = Modifier.fillMaxSize(), pagerState = state)
        }
    }
}

@HiltViewModel
class ImageGalleryViewModel @Inject constructor() : ViewModel() {

    private val _images: MutableStateFlow<List<Uri>> = MutableStateFlow(emptyList())
    val images = _images.asStateFlow()

    suspend fun onPostId(postId: String) {
        TODO("Not yet implemented")
    }
}
