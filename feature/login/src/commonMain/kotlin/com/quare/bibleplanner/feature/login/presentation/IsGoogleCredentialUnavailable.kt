package com.quare.bibleplanner.feature.login.presentation

fun interface IsGoogleCredentialUnavailable {
    operator fun invoke(error: Throwable?): Boolean
}
