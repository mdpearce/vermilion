package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.neaniesoft.vermilion.accounts.domain.UserAccountRepository
import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.api.RedditApiClientModule
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val userAccountRepository: UserAccountRepository,
    private val authUiProvider: AuthUiProvider
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = userAccountRepository.currentUserAccount

    fun onLoginClicked(): Intent {
        userAccountRepository.loginAsNewUser()

        return authUiProvider.getAuthIntent()
    }

    fun onAuthorizationResponse(response: AuthorizationResponse) {
        val authResponse = AuthResponse(response)
        userAccountRepository.handleAuthResponse(authResponse)
    }

    fun onAuthorizationError(exception: AuthorizationException) {

    }
}

interface AuthUiProvider {
    fun getAuthIntent(): Intent
}

@ActivityScoped
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
