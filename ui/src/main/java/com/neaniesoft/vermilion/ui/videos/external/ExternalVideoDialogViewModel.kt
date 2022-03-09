package com.neaniesoft.vermilion.ui.videos.external

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoUriResolverSupervisor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExternalVideoDialogViewModel @Inject constructor(
    private val resolverSupervisor: VideoUriResolverSupervisor
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<ExternalVideoDialogState>(ExternalVideoDialogState.Loading)
    val uiState = _uiState.asStateFlow()

    suspend fun onResolveExternalUri(uri: Uri) {
        _uiState.emit(ExternalVideoDialogState.Loading)

        resolverSupervisor.resolve(uri)
            .onSuccess {
                _uiState.emit(ExternalVideoDialogState.PlayUriState(it))
            }
            .onFailure {
                _uiState.emit(ExternalVideoDialogState.ErrorState(it))
            }
    }
}
