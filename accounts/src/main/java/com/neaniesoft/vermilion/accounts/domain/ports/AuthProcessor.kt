package com.neaniesoft.vermilion.accounts.domain.ports

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse

interface AuthProcessor {
    suspend fun updateAuthState(authResponse: AuthResponse<*, *>)
    fun invalidateAuthState()
}
