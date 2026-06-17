package com.quare.bibleplanner.feature.login.presentation

/**
 * Opens the platform flow that lets the user add a Google account to the device.
 *
 * Only Android has a meaningful implementation (the system "add account" screen); on the other
 * platforms it is a no-op, since [NoGoogleAccountClassifier] never reports a missing account there.
 */
fun interface AddGoogleAccountLauncher {
    operator fun invoke()
}
