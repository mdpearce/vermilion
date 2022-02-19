package com.neaniesoft.vermilion.api

import android.content.Context
import android.content.pm.PackageManager
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.neaniesoft.vermilion.api.interceptors.AuthorizationInterceptor
import com.neaniesoft.vermilion.api.interceptors.BasicAuthorizationInterceptor
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
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
        const val BASIC_AUTH = "basic_auth"
        const val BEARER_AUTH = "bearer_auth"

        private const val UNAUTHENTICATED_BASE_URL = "https://www.reddit.com/api/v1/"
        private const val AUTHENTICATED_BASE_URL = "https://oauth.reddit.com/"
        const val REDDIT_API_CLIENT_ID = "reddit_api_client_id"
        private const val USER_AGENT = "user_agent"
        private const val PLATFORM = "platform"
        private const val APP_ID = "app_id"
        private const val VERSION = "version"
        private const val DEVELOPER = "developer"
    }

    @Provides
    @Named(UNAUTHENTICATED)
    fun provideUnauthenticatedRetrofit(
        @Named(BASIC_AUTH) okHttpClient: OkHttpClient,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
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
    @Named(USER_AGENT)
    fun provideUserAgentInterceptor(@Named(USER_AGENT) userAgent: String): Interceptor =
        object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(
                    chain.request().newBuilder().header("User-Agent", userAgent).build()
                )
            }
        }

    @Provides
    @Named(BASIC_AUTH)
    fun provideUnauthenticatedOkHttpClient(
        @Named(USER_AGENT) userAgentInterceptor: Interceptor,
        basicAuthInterceptor: BasicAuthorizationInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(basicAuthInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()

    @Provides
    @Named(BASIC_AUTH)
    fun provideBasicAuthenticator(@Named(REDDIT_API_CLIENT_ID) clientId: String): Authenticator =
        Authenticator { _, response ->
            val credential = Credentials.basic(clientId, "")
            response.request().newBuilder().header("Authorization", credential).build()
        }

    @Provides
    @Named(AUTHENTICATED)
    fun provideAuthenticatedOkhttpClient(
        @Named(USER_AGENT) userAgentInterceptor: Interceptor,
        authInterceptor: AuthorizationInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()

    @Provides
    fun provideJacksonObjectMapper(): ObjectMapper =
        ObjectMapper().registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .registerModule(
                SimpleModule().addDeserializer(
                    Double::class.java,
                    BooleanDoubleCoercingDeserializer()
                )
            )

    @Provides
    fun provideJacksonConverterFactory(objectMapper: ObjectMapper): Converter.Factory =
        JacksonConverterFactory.create(objectMapper)

    @Provides
    fun provideAccessTokenService(@Named(UNAUTHENTICATED) retrofit: Retrofit): AccessTokenService =
        retrofit.create()

    @Provides
    @Named(REDDIT_API_CLIENT_ID)
    fun provideRedditApiClientId(@ApplicationContext context: Context): String =
        context.getString(R.string.reddit_api_client_id)

    @Provides
    @Named(USER_AGENT)
    fun provideUserAgent(
        @Named(PLATFORM) platform: String,
        @Named(APP_ID) appId: String,
        @Named(VERSION) version: String,
        @Named(DEVELOPER) developer: String
    ): String = "$platform:$appId:$version (by /u/$developer)"

    @Provides
    @Named(PLATFORM)
    fun providePlatform(): String = "android"

    @Provides
    @Named(APP_ID)
    fun provideAppId(@ApplicationContext context: Context): String = context.packageName

    @Provides
    @Named(VERSION)
    fun provideAppVersion(@ApplicationContext context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "vUnknown"
    }

    @Provides
    @Named(DEVELOPER)
    fun provideDeveloperName(): String = "NeaniesoftMichael"
}

class BooleanDoubleCoercingDeserializer : StdDeserializer<Double>(Double::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double {
        return p.valueAsDouble
    }
}
