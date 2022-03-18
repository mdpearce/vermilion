package com.neaniesoft.vermilion.postdetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.postdetails.domain.entities.Comment
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.utils.CoroutinesModule
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val logger by logger()

    private val _comments: MutableStateFlow<List<CommentKind>> = MutableStateFlow(emptyList())
    private val allComments = _comments.asStateFlow()
    val comments = allComments.map { it.filterNot { comment -> comment.isHidden } }

    private val networkActivityIdentifier = UUID.randomUUID().toString()

    val networkIsActive = commentRepository.networkActivityUpdates.filter {
        it.identifier == networkActivityIdentifier
    }.map { it.isActive }

    private val _scrollToEvents = MutableSharedFlow<Int>()
    val scrollToEvents = _scrollToEvents.asSharedFlow()

    suspend fun onPostId(postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                commentRepository.getCommentsForPost(postId).collect { comments ->
                    _comments.emit(comments)
                }
            }
        }
        withContext(dispatcher) {
            commentRepository.refreshIfRequired(
                postId,
                forceRefresh = false,
                networkActivityIdentifier = networkActivityIdentifier
            )
        }
    }

    fun onMoreCommentsClicked(stub: CommentStub) {
        viewModelScope.launch {
            val comments: List<CommentKind> = commentRepository.fetchAndInsertMoreCommentsFor(stub)

            _comments.emit(comments)
        }
    }

    fun onCommentNavDownClicked(firstVisibleItemIndex: Int) {
        viewModelScope.launch {
            val foundIndex = allComments.value.let { list ->
                list.subList(firstVisibleItemIndex, list.size)
                    .indexOfFirst { (it as? CommentKind.Full)?.comment?.depth == CommentDepth(0) }
            }
            if (foundIndex != -1) {
                val positionInCommentList = firstVisibleItemIndex + foundIndex + 1
                _scrollToEvents.emit(positionInCommentList)
            }
        }
    }

    fun onRefresh(postId: PostId) {
        viewModelScope.launch {
            commentRepository.refreshIfRequired(
                postId,
                forceRefresh = true,
                networkActivityIdentifier = networkActivityIdentifier
            )
        }
    }

    suspend fun onCommentId(commentId: CommentId, postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                commentRepository.getCommentThread(postId, commentId).collect { comments ->
                    _comments.emit(comments)
                }
            }
        }
        withContext(dispatcher) {
            commentRepository.refreshThread(
                postId = postId,
                threadId = commentId,
                networkActivityIdentifier
            )
        }
    }

    fun onCommentLongPressed(comment: Comment) {
        viewModelScope.launch {
            val comments = allComments.value
            val index = comments.indexOf(CommentKind.Full(comment))
            if (index != -1) {
                val updatedList =
                    comments.take(index) + CommentKind.Full(comment.copy(showActionsRow = !comment.showActionsRow)) + comments.subList(
                        index + 1,
                        comments.size
                    )
                _comments.emit(updatedList)
            }
        }
    }

    fun onCommentUpVoteClicked(comment: Comment) {
        viewModelScope.launch {
            commentRepository.toggleUpVote(comment)
        }
    }

    fun onCommentDownVoteClicked(comment: Comment) {
        viewModelScope.launch {
            commentRepository.toggleDownVote(comment)
        }
    }

    fun onCommentClicked(comment: Comment) {
        viewModelScope.launch {
            toggleCollapsedState(comment)
        }
    }

    private suspend fun toggleCollapsedState(comment: Comment) {
        val comments = allComments.value
        val isCollapsed = !comment.isCollapsed

        val collapsedIndex = comments.indexOf(CommentKind.Full(comment))
        if (collapsedIndex != -1) {
            val nextCommentIndex = (
                comments.subList(collapsedIndex + 1, comments.size)
                    .indexOfFirst { it.depth.value <= comment.depth.value } + collapsedIndex + 1
                ).takeIf { it != 0 }
                ?: comments.size

            val collapsedTree =
                listOf(CommentKind.Full(comment.copy(isCollapsed = isCollapsed))) + comments.subList(
                    collapsedIndex + 1,
                    nextCommentIndex
                ).map {
                    when (it) {
                        is CommentKind.Full -> CommentKind.Full(it.comment.copy(isHidden = isCollapsed))
                        is CommentKind.Stub -> CommentKind.Stub(it.stub.copy(isHidden = isCollapsed))
                        is CommentKind.Thread -> CommentKind.Thread(it.stub.copy(isHidden = isCollapsed))
                    }
                }

            val updatedList =
                comments.subList(0, collapsedIndex) + collapsedTree + comments.subList(
                    nextCommentIndex,
                    comments.size
                )

            _comments.emit(updatedList)
        }
    }
}
