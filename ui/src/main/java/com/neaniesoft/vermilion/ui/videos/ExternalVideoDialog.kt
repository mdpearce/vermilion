package com.neaniesoft.vermilion.ui.videos

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.github.michaelbull.result.Result
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Composable
fun ExternalVideoDialog(viewModel: ExternalVideoDialogViewModel = hiltViewModel()) {
}

interface VideoUriResolver {
    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverErrors>
}

sealed class VideoResolverErrors

@HiltViewModel
class ExternalVideoDialogViewModel @Inject constructor(
    private val resolvers: Set<@JvmSuppressWildcards VideoUriResolver>
) : ViewModel() {

}

@Singleton
class RedGifsVideoUriResolver @Inject constructor() : VideoUriResolver {
    override suspend fun resolve(uri: Uri): Result<Uri, VideoResolverErrors> {
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
