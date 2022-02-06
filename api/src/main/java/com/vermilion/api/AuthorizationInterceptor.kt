package com.vermilion.api

import com.vermilion.auth.http.AccessTokenService
import com.vermilion.auth.AuthorizationStore
import com.vermilion.auth.entities.DeviceAuthToken
import com.vermilion.auth.entities.UserAuthToken
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named

class AuthorizationInterceptor @Inject constructor(
    private val authorizationStore: AuthorizationStore,
    private val accessTokenService: AccessTokenService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        when (val token = authorizationStore.getToken(accessTokenService)) {
            is UserAuthToken -> {
                val authorizedRequest = chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()

                return chain.proceed(authorizedRequest)
            }
            is DeviceAuthToken -> {
                val authorizedRequest = chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()

                return chain.proceed(authorizedRequest)
            }
        }
    }
}

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
