package com.neaniesoft.vermilion.accounts.domain

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccountId
import com.neaniesoft.vermilion.accounts.domain.entities.UserName
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRecordRepository
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.utils.CoroutinesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserAccountRepository @Inject constructor(
    private val userAccountRecordRepository: UserAccountRecordRepository,
    private val authorizationStore: AuthorizationStore,
    private val authProcessor: AuthProcessor,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(dispatcher)

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

    fun handleAuthResponse(authResponse: AuthResponse<*, *>) {
        scope.launch { authProcessor.updateAuthState(authResponse) }
    }

    fun loginAsNewUser() {
        val account = UserAccount(UserAccountId(UUID.randomUUID()), UserName("Not set"))
        // This might lead to a race condition where the account is not saved before it is returned and used
        scope.launch {
            userAccountRecordRepository.saveUserAccount(account)
            authorizationStore.setLoggedInUserId(account.id.value)
            _currentUserAccount.emit(account)
        }
    }
}
