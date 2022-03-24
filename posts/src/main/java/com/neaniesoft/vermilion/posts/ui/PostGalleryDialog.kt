package com.neaniesoft.vermilion.posts.ui

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.ui.images.ImageGalleryDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun PostGalleryDialog(
    postId: PostId,
    onDismiss: () -> Unit,
    viewModel: PostGalleryDialogViewModel = hiltViewModel()
) {
    val images by viewModel.imagesForPost(postId).collectAsState(initial = emptyList())

    if (images.isNotEmpty()) {
        ImageGalleryDialog(images = images, onDismiss = onDismiss)
    }
}

@HiltViewModel
class PostGalleryDialogViewModel @Inject constructor(private val postRepository: PostRepository) :
    ViewModel() {

    fun imagesForPost(postId: PostId): Flow<List<Uri>> {
        return postRepository.postFlow(postId).map { post ->
            post.gallery.map { uriImage -> uriImage.uri }
        }
    }
}
