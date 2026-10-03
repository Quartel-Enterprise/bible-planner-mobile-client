package com.quare.bibleplanner.feature.login.presentation

import androidx.credentials.exceptions.NoCredentialException

internal class AndroidIsGoogleCredentialUnavailable : IsGoogleCredentialUnavailable {
    override fun invoke(error: Throwable?): Boolean = error is NoCredentialException
}
