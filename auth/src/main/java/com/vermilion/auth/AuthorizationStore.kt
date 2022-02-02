package com.vermilion.auth

interface AuthorizationStore {
    fun getToken(): TokenResponse
}

sealed class TokenResponse
data class AuthorizationToken(val token: Token, val refreshToken: Token? = null) : TokenResponse()
object TokenExpired : TokenResponse()
object TokenMissing : TokenResponse()

@JvmInline
value class Token(val value: String)