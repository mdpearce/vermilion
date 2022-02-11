package com.neaniesoft.vermilion.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun BottomNavBar(onUserAccountClicked: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        IconButton(onClick = onUserAccountClicked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_account_circle_24),
                contentDescription = null
            )
        }
    }
}
