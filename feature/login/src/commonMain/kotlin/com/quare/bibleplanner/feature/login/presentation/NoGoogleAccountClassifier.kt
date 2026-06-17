package com.quare.bibleplanner.feature.login.presentation

/**
 * Tells whether the [Throwable] that aborted a Google sign-in means the device has no Google
 * account available to pick.
 *
 * On Android this maps to `NoCredentialException`; on the other platforms there is no such
 * concept, so the implementation always returns `false`.
 */
fun interface NoGoogleAccountClassifier {
    operator fun invoke(error: Throwable?): Boolean
}
