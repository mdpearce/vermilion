package com.neaniesoft.vermilion.accounts.domain.entities

data class AuthResponse<T, E>(val response: T?, val exception: E?)
