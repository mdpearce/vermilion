package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.accounts.R
import com.neaniesoft.vermilion.ui.theme.VermilionTheme

@Composable
fun UserAccountScreen(viewModel: UserAccountViewModel = hiltViewModel()) {
    val currentUserAccount by viewModel.currentUser.collectAsState()

    if (currentUserAccount == null) {
        NotLoggedIn { viewModel.startLoginFlow() }
    } else {
        LoggedIn()
    }
}

@Composable
fun NotLoggedIn(onLogInClicked: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Button(onLogInClicked, Modifier.align(Alignment.Center)) {
            Text(
                text = stringResource(id = R.string.my_account_log_in),
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Composable
fun LoggedIn() {
}

@Preview
@Composable
fun NotLoggedInPreview() {
    VermilionTheme {
        NotLoggedIn {}
    }
}