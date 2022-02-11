package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import android.content.Intent

interface AuthUiProvider {
    fun getAuthIntent(): Intent
}
