package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.postdetails.data.CommentRepositoryResponse
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val database: VermilionDatabase,
    private val postDao: PostDao,
    private val commentRepository: CommentRepository,
    private val markdownParser: Parser,
    private val tabSupervisor: TabSupervisor,
    private val linkRouter: LinkRouter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _post: MutableStateFlow<PostDetailsState> = MutableStateFlow(Empty)
    val post: StateFlow<PostDetailsState> = _post.asStateFlow()

    private val _comments: MutableStateFlow<List<CommentKind>> = MutableStateFlow(emptyList())
    val comments: StateFlow<List<CommentKind>> = _comments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _scrollToEvents = MutableSharedFlow<Int>()
    val scrollToEvents = _scrollToEvents.asSharedFlow()

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents = _routeEvents.asSharedFlow()

    private val postId = PostId(
        savedStateHandle.get<String>("id")
            ?: throw IllegalStateException("Could not obtain post ID from saved state")
    )

    val restoredScrollPosition = flow {
        val position =
            tabSupervisor.scrollPositionForTab(ParentId(postId.value), TabType.POST_DETAILS)
        emit(position)
    }

    init {
        loadPostFromDb(postId)
        loadComments(false)
    }

    private fun loadPostFromDb(id: PostId) {
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

    private fun loadComments(forceRefresh: Boolean) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            val comments = commentRepository.getFlattenedCommentTreeForPost(postId, forceRefresh)

            comments.collect { response ->
                when (response) {
                    is CommentRepositoryResponse.UpdatedPost -> _post.emit(PostDetails(response.post))
                    is CommentRepositoryResponse.ListOfComments -> _comments.emit(response.comments)
                }
            }
            _isRefreshing.emit(false)
        }
    }

    fun onMoreCommentsClicked(stub: CommentStub) {
        viewModelScope.launch {
            val comments: List<CommentKind> = commentRepository.fetchAndInsertMoreCommentsFor(stub)

            _comments.emit(comments)
        }
    }

    suspend fun onScrollStateUpdated(scrollPosition: ScrollPosition) {
        tabSupervisor.updateScrollState(
            ParentId(postId.value),
            TabType.POST_DETAILS,
            scrollPosition
        )
    }

    fun refresh() {
        loadComments(true)
    }

    fun onCommentNavDownClicked(firstVisibleItemIndex: Int) {
        viewModelScope.launch {
            val foundIndex = comments.value.let { list ->
                list.subList(firstVisibleItemIndex, list.size)
                    .indexOfFirst { (it as? CommentKind.Full)?.comment?.depth == CommentDepth(0) }
            }
            if (foundIndex != -1) {
                val positionInCommentList = firstVisibleItemIndex + foundIndex + 1
                _scrollToEvents.emit(positionInCommentList)
            }
        }
    }

    fun onOpenUri(uri: Uri) {
        val route = linkRouter.routeForLink(uri)

        viewModelScope.launch { _routeEvents.emit(route) }
    }

    fun onUpVoteClicked(post: Post) {
    }
}

sealed class PostDetailsState

object Empty : PostDetailsState()
object Error : PostDetailsState()
data class PostDetails(val post: Post) : PostDetailsState()
