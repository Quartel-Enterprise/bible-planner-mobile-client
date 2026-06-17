package com.quare.bibleplanner.feature.login.presentation

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Sends the user to the system "add account" screen, pre-filtered to Google accounts.
 *
 * Launched from outside an Activity context, so it needs [Intent.FLAG_ACTIVITY_NEW_TASK]. Wrapped
 * in [runCatching] because the screen may be unavailable on some devices.
 */
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
