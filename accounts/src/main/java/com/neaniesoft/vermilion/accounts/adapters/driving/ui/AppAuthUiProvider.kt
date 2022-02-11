package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Intent
import androidx.core.net.toUri
import com.neaniesoft.vermilion.api.RedditApiClientModule
import dagger.hilt.android.scopes.ActivityRetainedScoped
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@ActivityRetainedScoped
class AppAuthUiProvider @Inject constructor(
    @Named(RedditApiClientModule.REDDIT_API_CLIENT_ID) private val clientId: String,
    private val authorizationService: AuthorizationService,
    private val configuration: AuthorizationServiceConfiguration
) : AuthUiProvider {
    companion object {
        private val REDIRECT_URL = "com.neaniesoft.vermilion://oauth2redirect".toUri()
        private const val RESPONSE_TYPE = "code"
        private const val DURATION = "permanent"
        private const val SCOPE = "read identity"
    }

    override fun getAuthIntent(): Intent {
        val stateString = stateString()

        val request =
            AuthorizationRequest.Builder(configuration, clientId, RESPONSE_TYPE, REDIRECT_URL)
                .setScope(SCOPE)
                .setState(stateString)
                .setAdditionalParameters(mapOf("duration" to DURATION))
                .build()

        return authorizationService.getAuthorizationRequestIntent(request)
    }

    private fun stateString() = UUID.randomUUID().toString()
}
