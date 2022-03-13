package com.neaniesoft.vermilion.postdetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.domain.PostVotingService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.utils.CoroutinesModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postVotingService: PostVotingService,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _post: MutableStateFlow<PostState> = MutableStateFlow(PostState.Empty)
    val post = _post.asStateFlow()

    fun onPostId(postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                postRepository.postFlow(postId).collect {
                    _post.emit(PostState.Post(it))
                }
            }
        }
    }

    fun onUpVoteClicked(post: Post) {
        viewModelScope.launch {
            postVotingService.toggleUpVote(post)
        }
    }

    fun onDownVoteClicked(post: Post) {
        viewModelScope.launch {
            postVotingService.toggleDownVote(post)
        }
    }
}
