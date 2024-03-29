package com.neaniesoft.vermilion.accounts.domain

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRepository
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.db.PostQueries
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import com.neaniesoft.vermilion.utils.CoroutinesModule
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserAccountService @Inject constructor(
    private val userAccountRepository: UserAccountRepository,
    private val authorizationStore: AuthorizationStore,
    private val authProcessor: AuthProcessor,
    private val postQueries: PostQueries,
    private val tabRepository: TabRepository,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(dispatcher)

    private val logger by logger()

    private val _currentUserAccount: MutableStateFlow<UserAccount?> =
        MutableStateFlow(currentLoggedInUserAccount())
    val currentUserAccount: StateFlow<UserAccount?> = _currentUserAccount.asStateFlow()

    private fun getLoggedInUserId() = authorizationStore.getCurrentLoggedInUserAccountId()

    private fun currentLoggedInUserAccount(): UserAccount? {
        val currentUserId = getLoggedInUserId()
        return if (currentUserId != null) {
            UserAccount(
                UserAccountId(currentUserId),
                UserName("unknown")
            )
        } else {
            null
        }
    }

    fun handleAuthResponse(authResponse: AuthResponse<AuthorizationResponse, AuthorizationException>) {
        scope.launch {
            authProcessor.updateAuthState(authResponse)
                .onSuccess {
                    loginAsNewUser()
                }.onFailure {
                    logger.warnIfEnabled(it.cause) { "Auth failure" }
                }
        }
    }

    fun isAuthorized(): Boolean {
        return authProcessor.isAuthorized()
    }

    private fun loginAsNewUser() {
        val account = UserAccount(UserAccountId(UUID.randomUUID()), UserName("Not set"))
        // This might lead to a race condition where the account is not saved before it is returned and used
        scope.launch {
            postQueries.deleteAll()
            tabRepository.removeAll()
            userAccountRepository.saveUserAccount(account)
                .onFailure { error -> logger.errorIfEnabled(error.cause) { "Error saving user account to disk. $error" } }
                .onSuccess { userAccount -> logger.debugIfEnabled { "Saved user account with id ${userAccount.id}" } }
            authorizationStore.setLoggedInUserId(account.id.value)
            _currentUserAccount.emit(account)
        }
    }

    fun logout() {
        logger.debugIfEnabled { "Logging out" }
        scope.launch {
            postQueries.deleteAll() // TODO wrap the dao in an adapter to avoid using it directly here
            tabRepository.removeAll()
            authorizationStore.setLoggedInUserId(null)
            authProcessor.invalidateAuthState()
            val currentAccount = currentUserAccount.value
            if (currentAccount != null) {
                userAccountRepository.clearAccount(currentAccount)
            }
            _currentUserAccount.emit(null)
        }
    }
}
