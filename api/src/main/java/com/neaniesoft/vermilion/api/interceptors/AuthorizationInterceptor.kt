package com.neaniesoft.vermilion.api.interceptors

import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val authorizationStore: AuthorizationStore,
    private val accessTokenService: AccessTokenService,
    private val authState: AuthState,
    private val authorizationService: AuthorizationService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        if (authState.isAuthorized) {
            val token: CompletableDeferred<String> = CompletableDeferred()
            authState.performActionWithFreshTokens(authorizationService) { accessToken, idToken, ex ->
                if (ex != null) {
                    throw ex
                }
                val validAccessToken =
                    requireNotNull(accessToken) { "Access token was null, but no authorization exception occurred" }
                token.complete(validAccessToken)
            }
            val freshToken = runBlocking {
                token.await()
            }

            return chain.proceed(
                chain.request().newBuilder()
                    .header("Authorization", "bearer $freshToken")
                    .build()
            )
        } else {
            val token = authorizationStore.getDeviceToken(accessTokenService)
            return chain.proceed(
                chain.request().newBuilder()
                    .header("Authorization", "bearer ${token.token.value}")
                    .build()
            )
        }
    }
}
