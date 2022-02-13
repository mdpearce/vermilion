package com.neaniesoft.vermilion.auth.errors

import java.io.IOException

sealed class AuthError(throwable: Throwable? = null) : Error(throwable)

object InvalidDeviceId : AuthError()
data class UnsuccessfulTokenRequest(val code: Int) : AuthError()
class AuthTokenServiceIoError(throwable: IOException) : AuthError(throwable)
class UnhandledError(throwable: Throwable) : AuthError(throwable)
object EmptyBody : AuthError()
