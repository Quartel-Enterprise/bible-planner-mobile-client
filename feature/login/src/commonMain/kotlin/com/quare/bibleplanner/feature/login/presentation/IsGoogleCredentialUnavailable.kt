package com.quare.bibleplanner.feature.login.presentation

// Why: Android Credential Manager answers the same for no Google account and a rejected
// request (OAuth client, unhealthy Play services), with no permission-free way to
// tell them apart; other platforms always return false.
fun interface IsGoogleCredentialUnavailable {
    operator fun invoke(error: Throwable?): Boolean
}
