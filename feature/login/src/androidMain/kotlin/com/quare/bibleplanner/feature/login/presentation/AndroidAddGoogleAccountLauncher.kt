package com.quare.bibleplanner.feature.login.presentation

import android.content.Context
import android.content.Intent
import android.provider.Settings

internal class AndroidAddGoogleAccountLauncher(
    private val context: Context,
) : AddGoogleAccountLauncher {
    override fun invoke() {
        runCatching {
            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf(GOOGLE_ACCOUNT_TYPE))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private companion object {
        const val GOOGLE_ACCOUNT_TYPE = "com.google"
    }
}
