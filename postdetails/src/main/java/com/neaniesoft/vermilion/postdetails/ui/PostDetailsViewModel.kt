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
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
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
    private val tabSupervisor: TabSupervisor,
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

    private val initialScrollPosition = ScrollPosition(
        savedStateHandle.get<Int>("initialScrollIndex") ?: 0
    )

    private val _scrollPosition = MutableStateFlow(initialScrollPosition)
    val initialScrollPositionState: StateFlow<ScrollPosition> = _scrollPosition.asStateFlow()

    private val _scrollUpdates: MutableStateFlow<Int> = MutableStateFlow(0)
    private val scrollUpdates = _scrollUpdates.asStateFlow()

    init {
        loadPost(postId)
        loadComments(postId)
        setUpScrollListener()
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
        viewModelScope.launch {
            val comments: List<CommentKind> = commentRepository.fetchAndInsertMoreCommentsFor(stub)

            _comments.emit(comments)
        }
    }

    private fun setUpScrollListener() {
        viewModelScope.launch {
            scrollUpdates.collect {
                tabSupervisor.updateScrollStateForPostDetailsTab(
                    postId = postId,
                    scrollPosition = ScrollPosition(it)
                )
            }
        }
    }

    fun onScrollStateUpdated(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        viewModelScope.launch { _scrollUpdates.emit(firstVisibleItemIndex) }
    }
}

sealed class PostDetailsState

object Empty : PostDetailsState()
object Error : PostDetailsState()
data class PostDetails(val post: Post) : PostDetailsState()
