package com.quare.bibleplanner.feature.login.presentation

import androidx.credentials.exceptions.NoCredentialException

/**
 * Credential Manager raises [NoCredentialException] whenever it cannot hand over a Google
 * credential, without saying why: the device may have no Google account, or the request may have
 * been rejected — most often because no Android OAuth client matches this package and signing
 * certificate. Since Android 8.0 the accounts on the device cannot settle it either
 * (`AccountManager.getAccountsByType` only lists accounts whose authenticator made them visible to
 * the caller, and `GET_ACCOUNTS` is ignored), so both are reported as one recoverable failure.
 */
internal class AndroidIsGoogleCredentialUnavailable : IsGoogleCredentialUnavailable {
    override fun invoke(error: Throwable?): Boolean = error is NoCredentialException
}
