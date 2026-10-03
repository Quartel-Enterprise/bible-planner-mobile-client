package com.quare.bibleplanner.feature.login.presentation

// Why: only Android has a real flow (system add-account screen); elsewhere it is a no-op
// because IsGoogleCredentialUnavailable never reports a missing credential there.
fun interface AddGoogleAccountLauncher {
    operator fun invoke()
}
