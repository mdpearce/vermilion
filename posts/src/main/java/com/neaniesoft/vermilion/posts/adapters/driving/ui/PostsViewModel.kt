package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.posts.domain.ports.FrontPage
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private val _state: MutableStateFlow<PostsScreenState> = MutableStateFlow(PostsScreenState.Empty())
    val state: StateFlow<PostsScreenState> = _state

    init {
        fetchPostsFromRepository()
    }

    private fun fetchPostsFromRepository() {
        viewModelScope.launch(Dispatchers.IO) {
            val posts = postRepository.postsForCommunity(FrontPage)
            _state.emit(PostsScreenState.Posts(posts.results, false))
        }
    }
}