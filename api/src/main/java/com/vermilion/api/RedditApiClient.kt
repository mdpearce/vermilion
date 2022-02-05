package com.vermilion.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.vermilion.auth.AccessTokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.create
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class RedditApiClientModule {
    companion object {
        const val UNAUTHENTICATED = "unauthenticated"
        const val AUTHENTICATED = "authenticated"

        private const val UNAUTHENTICATED_BASE_URL = "https://www.reddit.com/api/v1/"
        private const val AUTHENTICATED_BASE_URL = "https://oauth.reddit.com/"
    }

    @Provides
    @Named(UNAUTHENTICATED)
    fun provideUnauthenticatedRetrofit(converterFactory: Converter.Factory): Retrofit = Retrofit.Builder()
        .baseUrl(UNAUTHENTICATED_BASE_URL)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    @Named(AUTHENTICATED)
    fun provideAuthenticatedRetrofit(
        @Named(AUTHENTICATED) okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AUTHENTICATED_BASE_URL)
        .addConverterFactory(converterFactory)
        .client(okHttpClient)
        .build()

    @Provides
    @Named(AUTHENTICATED)
    fun provideAuthenticatedOkhttpClient(authorizationInterceptor: AuthorizationInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .build()

    @Provides
    fun provideJacksonObjectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Provides
    fun provideJacksonConverterFactory(objectMapper: ObjectMapper): Converter.Factory =
        JacksonConverterFactory.create(objectMapper)

    @Provides
    fun provideAccessTokenService(@Named(UNAUTHENTICATED) retrofit: Retrofit): AccessTokenService = retrofit.create()
}

data class ListingResponse(
    @JsonProperty("after") val after: String?,
    @JsonProperty("dist") val dist: Int,
    @JsonProperty("before") val before: String?
)