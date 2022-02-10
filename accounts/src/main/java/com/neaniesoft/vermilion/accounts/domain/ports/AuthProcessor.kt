package com.neaniesoft.vermilion.accounts.domain.ports

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse

interface AuthProcessor {
    fun updateAuthState(authResponse: AuthResponse<*>)
}
