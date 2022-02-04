package com.vermilion.api

import com.vermilion.auth.AccessTokenService
import com.vermilion.auth.AuthorizationStore
import com.vermilion.auth.DeviceAuthorizationToken
import com.vermilion.auth.UserAuthorizationToken
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val authorizationStore: AuthorizationStore,
    private val accessTokenService: AccessTokenService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        when (val token = authorizationStore.getToken(accessTokenService)) {
            is UserAuthorizationToken -> {
                val authorizedRequest = chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()

                return chain.proceed(authorizedRequest)
            }
            is DeviceAuthorizationToken -> {
                val authorizedRequest = chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()

                return chain.proceed(authorizedRequest)
            }
        }
    }
}
