package com.quare.bibleplanner.feature.login.presentation

import androidx.credentials.exceptions.NoCredentialException

/**
 * Reports a missing Google account when the credential request fails with
 * [NoCredentialException], i.e. the device has no Google account to offer.
 */
internal class AndroidNoGoogleAccountClassifier : NoGoogleAccountClassifier {
    override fun invoke(error: Throwable?): Boolean = error is NoCredentialException
}
