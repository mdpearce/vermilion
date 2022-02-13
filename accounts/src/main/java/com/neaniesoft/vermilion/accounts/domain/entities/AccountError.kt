package com.neaniesoft.vermilion.accounts.domain.entities

sealed class AccountError(override val cause: Throwable? = null) : Error()

class DatabaseError(cause: Throwable) : AccountError(cause)
