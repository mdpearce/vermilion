package com.neaniesoft.vermilion.ui.videos.external.resolver

import com.neaniesoft.vermilion.ui.videos.external.imgur.ImgurGifvVideoResolver
import com.neaniesoft.vermilion.ui.videos.external.redgifs.RedGifsVideoUriResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class VideoUriResolversModule {
    @Binds
    @IntoSet
    abstract fun bindRedGifsVideoResolver(resolver: RedGifsVideoUriResolver): VideoUriResolver

    @Binds
    @IntoSet
    abstract fun bindImgurGifvVideoResolver(resolver: ImgurGifvVideoResolver): VideoUriResolver
}
