package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import io.ktor.client.plugins.HttpRequestTimeoutException

/**
 * Maps the [Throwable] that aborted a social login into the [LoginError] that
 * decides which message the user sees.
 */
internal class ThrowableToLoginErrorMapper {
    operator fun invoke(throwable: Throwable?): LoginError = when {
        throwable is HttpRequestTimeoutException -> LoginError.CONNECTION
        throwable.isMissingProviderEmail() -> LoginError.EMAIL_REQUIRED
        else -> LoginError.GENERIC
    }

    /**
     * Whether [this] is the auth server rejecting the sign-in because the provider returned no email —
     * e.g. Apple sign-in with the email scope withheld, which Supabase blocks while "Allow users
     * without an email" is off. The server reports it with [MISSING_PROVIDER_EMAIL_MESSAGE].
     */
    private fun Throwable?.isMissingProviderEmail(): Boolean =
        this?.message?.contains(MISSING_PROVIDER_EMAIL_MESSAGE, ignoreCase = true) == true

    private companion object {
        const val MISSING_PROVIDER_EMAIL_MESSAGE = "Error getting user email from external provider"
    }
}
