package com.neaniesoft.vermilion.posts.adapters.driving.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.neaniesoft.vermilion.api.RedditApiClientModule
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    @Named(RedditApiClientModule.AUTHENTICATED) private val okhttpClient: OkHttpClient
) : ViewModel() {
    private val _state: MutableStateFlow<PostsScreenState> =
        MutableStateFlow(PostsScreenState.Empty())
    val state: StateFlow<PostsScreenState> = _state

    init {
        fetchPostsFromRepository()
    }

    fun getImageLoader(context: Context): ImageLoader =
        ImageLoader(context).newBuilder().okHttpClient(okhttpClient).build()

    private fun fetchPostsFromRepository() {
        viewModelScope.launch(Dispatchers.IO) {
            val posts = postRepository.postsForCommunity(FrontPage)
            _state.emit(PostsScreenState.Posts(posts.results, false))
        }
    }
}
