package com.vermilion.api.interceptors

import com.vermilion.api.RedditApiClientModule
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named

class BasicAuthorizationInterceptor @Inject constructor(
    @Named(RedditApiClientModule.REDDIT_API_CLIENT_ID) private val clientId: String
) : Interceptor {
    private val credentials = Credentials.basic(clientId, "")
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", credentials)
                .build()
        )
    }
}
