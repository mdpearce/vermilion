package com.neaniesoft.vermilion.accounts.adapters.driven.auth

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import javax.inject.Inject

class AppAuthProcessor @Inject constructor() : AuthProcessor {
    override fun updateAuthState(authResponse: AuthResponse<*>) {
        TODO("Not yet implemented")
    }
}
