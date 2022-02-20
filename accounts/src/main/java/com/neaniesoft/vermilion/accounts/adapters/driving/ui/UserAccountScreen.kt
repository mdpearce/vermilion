package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.accounts.R
import com.neaniesoft.vermilion.ui.theme.VermilionTheme
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

@Composable
fun UserAccountScreen(viewModel: UserAccountViewModel = hiltViewModel()) {
    val currentUserAccount by viewModel.currentUser.collectAsState()

    val authLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val data = requireNotNull(result.data)
            val response = AuthorizationResponse.fromIntent(data)
            val exception = AuthorizationException.fromIntent(data)

            if (exception != null) {
                Log.e("LaunchAuthFlow", exception.error.toString())
                Log.e("LaunchAuthFlow", exception.errorDescription.toString())
            }
            viewModel.onAuthorizationResponse(response, exception)
        }
    )

    LaunchedEffect(key1 = currentUserAccount) {
        viewModel.authIntents.collect { intent ->
            authLauncher.launch(intent)
        }
    }

    if (currentUserAccount == null) {
        NotLoggedIn {
            viewModel.onLoginClicked()
        }
    } else {
        LoggedIn {
            viewModel.onLogoutClicked()
        }
    }
}

@Composable
fun NotLoggedIn(onLogInClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Button(onLogInClicked, Modifier.align(Alignment.TopCenter)) {
            Text(
                text = stringResource(id = R.string.my_account_log_in),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Composable
fun LoggedIn(onLogOutClicked: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Button(onLogOutClicked, Modifier.align(Alignment.TopCenter)) {
            Text(
                text = stringResource(id = R.string.my_account_log_out),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
fun NotLoggedInPreview() {
    VermilionTheme {
        NotLoggedIn {}
    }
}
