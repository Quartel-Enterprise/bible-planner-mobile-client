package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import io.ktor.client.plugins.HttpRequestTimeoutException

internal class ThrowableToLoginErrorMapper {
    operator fun invoke(throwable: Throwable?): LoginError = when {
        throwable is HttpRequestTimeoutException -> LoginError.CONNECTION
        throwable.isMissingProviderEmail() -> LoginError.EMAIL_REQUIRED
        else -> LoginError.GENERIC
    }

    private fun Throwable?.isMissingProviderEmail(): Boolean =
        this?.message?.contains(MISSING_PROVIDER_EMAIL_MESSAGE, ignoreCase = true) == true

    private companion object {
        const val MISSING_PROVIDER_EMAIL_MESSAGE = "Error getting user email from external provider"
    }
}
