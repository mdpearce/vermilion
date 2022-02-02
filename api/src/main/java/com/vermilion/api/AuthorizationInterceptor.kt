package com.vermilion.api

import com.vermilion.auth.AuthorizationStore
import com.vermilion.auth.AuthorizationToken
import com.vermilion.auth.TokenExpired
import com.vermilion.auth.TokenMissing
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val authorizationStore: AuthorizationStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        when (val token = authorizationStore.getToken()) {
            is TokenMissing -> throw TokenMissingException()
            is TokenExpired -> throw TokenExpiredException()
            is AuthorizationToken -> {
                val authorizedRequest = chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()

                return chain.proceed(authorizedRequest)
            }
        }
    }
}

class TokenMissingException() : Exception("No authorization token was found in encrypted storage")
class TokenExpiredException() : Exception("Authorization token has expired")