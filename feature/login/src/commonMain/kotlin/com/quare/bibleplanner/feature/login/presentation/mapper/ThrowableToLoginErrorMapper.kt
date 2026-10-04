package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import io.ktor.client.plugins.HttpRequestTimeoutException

internal class ThrowableToLoginErrorMapper {
    operator fun invoke(throwable: Throwable?): LoginError = when {
        throwable is HttpRequestTimeoutException -> LoginError.CONNECTION
        throwable.isMissingProviderEmail() -> LoginError.EMAIL_REQUIRED
        else -> LoginError.GENERIC
    }

    /*
     * Why: Supabase rejects sign-ins without a provider email (e.g. Apple with the email scope
     * withheld) while "Allow users without an email" is off; matched by message since GoTrue
     * exposes no error code yet (supabase/auth#2584).
     */
    private fun Throwable?.isMissingProviderEmail(): Boolean =
        this?.message?.contains(MISSING_PROVIDER_EMAIL_MESSAGE, ignoreCase = true) == true

    private companion object {
        const val MISSING_PROVIDER_EMAIL_MESSAGE = "Error getting user email from external provider"
    }
}
