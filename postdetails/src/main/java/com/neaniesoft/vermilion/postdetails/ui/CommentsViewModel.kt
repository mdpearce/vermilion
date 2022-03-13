package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.postdetails.data.CommentRepository
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentDepth
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentId
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentKind
import com.neaniesoft.vermilion.postdetails.domain.entities.CommentStub
import com.neaniesoft.vermilion.posts.domain.LinkRouter
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
    private val linkRouter: LinkRouter,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val logger by logger()

    private val _comments: MutableStateFlow<List<CommentKind>> = MutableStateFlow(emptyList())
    val comments = _comments.asStateFlow()

    private val networkActivityIdentifier = UUID.randomUUID().toString()

    val networkIsActive = commentRepository.networkActivityUpdates.filter {
        it.identifier == networkActivityIdentifier
    }.map { it.isActive }

    private val _scrollToEvents = MutableSharedFlow<Int>()
    val scrollToEvents = _scrollToEvents.asSharedFlow()

    private val _routeEvents: MutableSharedFlow<String> = MutableSharedFlow()
    val routeEvents = _routeEvents.asSharedFlow()

    suspend fun onPostId(postId: PostId) {
        viewModelScope.launch {
            withContext(dispatcher) {
                commentRepository.getCommentsForPost(postId).collect { comments ->
                    _comments.emit(comments)
                }
            }
        }
        commentRepository.refreshIfRequired(
            postId,
            forceRefresh = false,
            networkActivityIdentifier = networkActivityIdentifier
        )
    }

    fun onMoreCommentsClicked(stub: CommentStub) {
        viewModelScope.launch {
            val comments: List<CommentKind> = commentRepository.fetchAndInsertMoreCommentsFor(stub)

            _comments.emit(comments)
        }
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

    }
}
