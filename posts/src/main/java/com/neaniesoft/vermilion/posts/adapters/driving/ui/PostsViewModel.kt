package com.neaniesoft.vermilion.posts.adapters.driving.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(): ViewModel() {
    private val _state: MutableStateFlow<PostsScreenState> = MutableStateFlow(PostsScreenState.Empty())
    val state: StateFlow<PostsScreenState> = _state
}