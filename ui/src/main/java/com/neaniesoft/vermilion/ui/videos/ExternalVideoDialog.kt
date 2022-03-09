package com.neaniesoft.vermilion.ui.videos

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.api.RedditApiClientModule
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@ExperimentalMaterialApi
@Composable
fun ExternalVideoDialog(
    unresolvedUri: Uri,
    onDismiss: () -> Unit,
    viewModel: ExternalVideoDialogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val videoPlayerState = rememberVideoPlayerState()

    LaunchedEffect(key1 = unresolvedUri) {
        viewModel.onResolveExternalUri(unresolvedUri)
    }


    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        when (val currentState = uiState) {
            is ExternalVideoDialogState.Loading -> {} // TODO: Put a progress spinner here
            is ExternalVideoDialogState.ErrorState -> {
                Log.e(
                    "ExternalVideoDialog",
                    "Error: ${currentState.error}"
                ) // TODO Handle this better
            }
            is ExternalVideoDialogState.PlayUriState -> {
                VideoPlayer(
                    state = videoPlayerState,
                    video = Video.UriVideo(currentState.uri),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

interface VideoUriResolver {
    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError>
    fun handles(uri: Uri): Boolean
}

sealed class VideoResolverError
object NoRegisteredResolvers : VideoResolverError()
object InvalidUriForResolver : VideoResolverError()
data class ApiError(val cause: Throwable) : VideoResolverError()

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

sealed class ExternalVideoDialogState {
    object Loading : ExternalVideoDialogState()
    data class ErrorState(val error: VideoResolverError) : ExternalVideoDialogState()
    data class PlayUriState(val uri: Uri) : ExternalVideoDialogState()
}

@Singleton
class RedGifsVideoUriResolver @Inject constructor(
    private val api: RedGifsApi
) : VideoUriResolver {
    override suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError> {
        Log.d("RedGifsVideoUriResolver", "host: ${uri.host}, pathSegments: ${uri.pathSegments}")
        return if (handles(uri)) {
            val pathSegments = uri.pathSegments
            getVideoUri(pathSegments[1])
        } else {
            Err(InvalidUriForResolver)
        }
    }

    private suspend fun getVideoUri(id: String): Result<Uri, VideoResolverError> {
        return runCatching {
            api.getGif(id)
        }.mapError { ApiError(it) }
            .map { response ->
                response.gif.urls.hd.toUri()
            }
    }

    override fun handles(uri: Uri): Boolean {
        return (uri.host == "redgifs.com" || uri.host == "www.redgifs.com")
            && uri.pathSegments.size == 2
            && uri.pathSegments[0] == "watch"
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

@Module
@InstallIn(SingletonComponent::class)
class RedGifsApiModule {
    companion object {
        const val RED_GIFS_API = "red_gifs_api"
        const val RED_GIFS_BASE_URL = "https://api.redgifs.com/"
    }

    @Provides
    fun provideRedGifsApi(@Named(RED_GIFS_API) retrofit: Retrofit): RedGifsApi {
        return retrofit.create()
    }

    @Provides
    @Named(RED_GIFS_API)
    fun provideRedGifsRetrofit(
        @Named(RedditApiClientModule.NO_AUTH) okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RED_GIFS_BASE_URL)
            .addConverterFactory(converterFactory)
            .build()
    }
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

@Singleton
class VideoUriResolverSupervisor @Inject constructor(
    private val resolvers: Set<@JvmSuppressWildcards VideoUriResolver>
) {
    fun canAnyResolverHandle(uri: Uri): Boolean {
        return resolvers.find { it.handles(uri) } != null
    }

    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError> {
        return resolvers.find { it.handles(uri) }?.resolve(uri) ?: Err(NoRegisteredResolvers)
    }
}

