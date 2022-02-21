package com.neaniesoft.vermilion.postdetails.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val database: VermilionDatabase,
    private val postDao: PostDao,
    private val commentRepository: CommentRepository,
    private val markdownParser: Parser,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _post: MutableStateFlow<PostDetailsState> = MutableStateFlow(Empty)
    val post: StateFlow<PostDetailsState> = _post.asStateFlow()

    private val _comments: MutableStateFlow<List<CommentKind>> = MutableStateFlow(emptyList())
    val comments: StateFlow<List<CommentKind>> = _comments.asStateFlow()

    private val postId = PostId(
        savedStateHandle.get<String>("id")
            ?: throw IllegalStateException("Could not obtain post ID from saved state")
    )

    init {
        loadPost(postId)
        loadComments(postId)
    }

    private fun loadPost(id: PostId) {
        viewModelScope.launch {
            val post = database.withTransaction {
                postDao.postWithId(id.value)
            }
            if (post == null) {
                _post.emit(Error)
            } else {
                _post.emit(PostDetails(post.toPost(markdownParser)))
            }
        }
    }

    private fun loadComments(id: PostId) {
        viewModelScope.launch {
            val comments = commentRepository.getFlattenedCommentTreeForPost(id)

            comments.collect {
                _comments.emit(it)
            }
        }
    }

    fun onMoreCommentsClicked(stub: CommentStub) {
    }
}

sealed class PostDetailsState

object Empty : PostDetailsState()
object Error : PostDetailsState()
data class PostDetails(val post: Post) : PostDetailsState()
