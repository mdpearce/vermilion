package com.neaniesoft.vermilion.ui.videos

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

@Composable
fun ExternalVideoDialog(viewModel: ExternalVideoDialogViewModel = hiltViewModel()) {
}

interface VideoUriResolver {
    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverErrors>
    fun handles(uri: Uri): Boolean
}

sealed class VideoResolverErrors
object NoRegisteredResolvers : VideoResolverErrors()

@HiltViewModel
class ExternalVideoDialogViewModel @Inject constructor(
    private val resolvers: Set<@JvmSuppressWildcards VideoUriResolver>
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<ExternalVideoDialogState>(ExternalVideoDialogState.Loading)
    val uiState = _uiState.asStateFlow()

    suspend fun onResolveExternalUri(uri: Uri) {
        _uiState.emit(ExternalVideoDialogState.Loading)

        val resolver = resolvers.firstOrNull { it.handles(uri) }
        if (resolver != null) {
            resolver.resolve(uri)
                .onSuccess {
                    _uiState.emit(ExternalVideoDialogState.PlayUriState(it))
                }
                .onFailure {
                    _uiState.emit(ExternalVideoDialogState.ErrorState(it))
                }
        } else {
            _uiState.emit(ExternalVideoDialogState.ErrorState(NoRegisteredResolvers))
        }
    }
}

sealed class ExternalVideoDialogState {
    object Loading : ExternalVideoDialogState()
    data class ErrorState(val error: VideoResolverErrors) : ExternalVideoDialogState()
    data class PlayUriState(val uri: Uri) : ExternalVideoDialogState()
}

@Singleton
class RedGifsVideoUriResolver @Inject constructor() : VideoUriResolver {
    override suspend fun resolve(uri: Uri): Result<Uri, VideoResolverErrors> {
        TODO("Not yet implemented")
    }

    override fun handles(uri: Uri): Boolean {
        TODO("Not yet implemented")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class VideoUriResolversModule {
    @Binds
    @IntoSet
    abstract fun bindRedGifsVideoResolver(resolver: RedGifsVideoUriResolver): VideoUriResolver
}

interface RedGifsApi {
    @GET("/v2/gifs/{id}")
    suspend fun getGif(@Path("id") id: String): GifResponse
}

data class GifResponse(
    @JsonProperty("gif") val gif: GifInfo
)

data class GifInfo(
    @JsonProperty("id") val id: String,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("urls") val urls: MediaInfo
)

data class MediaInfo(
    @JsonProperty("sd") val sd: String,
    @JsonProperty("hd") val hd: String
)
