package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.neaniesoft.vermilion.posts.adapters.driven.PostsPagingSource
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    val pageFlow = Pager(PagingConfig(pageSize = 20)) {
        PostsPagingSource(postRepository, FrontPage)
    }.flow.cachedIn(viewModelScope)
}
