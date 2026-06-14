package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.feature.login.presentation.model.LoginError
import io.ktor.client.plugins.HttpRequestTimeoutException

/**
 * Maps the [Throwable] that aborted a social login into the [LoginError] that
 * decides which message the user sees.
 */
internal class ThrowableToLoginErrorMapper {
    operator fun invoke(throwable: Throwable?): LoginError = when (throwable) {
        is HttpRequestTimeoutException -> LoginError.CONNECTION
        else -> LoginError.GENERIC
    }
}
