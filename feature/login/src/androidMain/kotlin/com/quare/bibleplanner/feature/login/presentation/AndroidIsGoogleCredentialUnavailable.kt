package com.quare.bibleplanner.feature.login.presentation

import androidx.credentials.exceptions.NoCredentialException

/*
 * Why: NoCredentialException does not say if the device lacks a Google account or no OAuth client
 * matches the package/cert, and since Android 8.0 AccountManager cannot tell either, so both are
 * one recoverable failure.
 */
internal class AndroidIsGoogleCredentialUnavailable : IsGoogleCredentialUnavailable {
    override fun invoke(error: Throwable?): Boolean = error is NoCredentialException
}
