package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.neaniesoft.vermilion.accounts.domain.UserAccountRepository
import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

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

    fun onAuthorizationResponse(
        response: AuthorizationResponse?,
        exception: AuthorizationException?
    ) {
        val authResponse = AuthResponse(response, exception)
        userAccountRepository.handleAuthResponse(authResponse)
    }

    fun onLogoutClicked() {
        userAccountRepository.logout()
    }
}

