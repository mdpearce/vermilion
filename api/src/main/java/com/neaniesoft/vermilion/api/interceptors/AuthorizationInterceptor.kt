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
                if (ex != null) {
                    val errorMessage =
                        "Error getting fresh token: ${ex.error}, code: ${ex.code}, desc: ${ex.errorDescription}"
                    logger.errorIfEnabled { errorMessage }
                    token.completeExceptionally(ex)
                } else {
                    if (accessToken == null) {
                        token.completeExceptionally(NullPointerException("Access token was null, but no authorization exception occurred"))
                    } else {
                        token.complete(accessToken)
                    }
                }
            }

            return try {
                val freshToken = runBlocking {
                    token.await()
                }
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "bearer $freshToken")
                        .build()
                )
            } catch (e: Throwable) {
                Response.Builder().code(500).body(
                    ResponseBody.create(
                        null,
                        "Error obtaining token.\n$e (${e.message}) caused by ${e.cause} (${e.cause?.message})"
                    )
                ).build()
            }
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
