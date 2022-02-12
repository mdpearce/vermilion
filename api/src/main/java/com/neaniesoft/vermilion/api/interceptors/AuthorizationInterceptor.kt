package com.neaniesoft.vermilion.api.interceptors

import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val authorizationStore: AuthorizationStore,
    private val accessTokenService: AccessTokenService,
    private val authState: AuthState,
    private val authorizationService: AuthorizationService
) : Interceptor {
    private val logger by logger()

    override fun intercept(chain: Interceptor.Chain): Response {

        if (authState.isAuthorized) {
            val token: CompletableDeferred<String> = CompletableDeferred()
            authState.performActionWithFreshTokens(
                authorizationService,
                ClientSecretBasic("")
            ) { accessToken, _, ex ->
                logger.debugIfEnabled { "Got token" }
                if (ex != null) {
                    logger.errorIfEnabled { "Error getting fresh token: ${ex.error}" }
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
            logger.debugIfEnabled { "Not authorized, using device token" }
            val tokenResult = authorizationStore.getDeviceToken(accessTokenService)

            val token = tokenResult.get()
            return if (token != null) {
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "bearer ${token.token.value}").build()
                )
            } else {
                val error = tokenResult.getError()
                Response.Builder()
                    .code(500)
                    .body(ResponseBody.create(null, "$error error caused by ${error?.cause}"))
                    .build()
            }
        }
    }
}
