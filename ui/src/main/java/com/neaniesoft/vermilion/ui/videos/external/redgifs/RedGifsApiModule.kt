package com.neaniesoft.vermilion.ui.videos.external.redgifs

import com.neaniesoft.vermilion.api.RedditApiClientModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named

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
