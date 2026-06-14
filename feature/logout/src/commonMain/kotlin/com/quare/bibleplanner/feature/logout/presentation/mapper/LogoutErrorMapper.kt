package com.quare.bibleplanner.feature.logout.presentation.mapper

import bibleplanner.feature.logout.generated.resources.Res
import bibleplanner.feature.logout.generated.resources.logout_error_message
import com.quare.bibleplanner.feature.logout.presentation.model.LogoutError
import org.jetbrains.compose.resources.StringResource

internal class LogoutErrorMapper {
    fun map(error: LogoutError): StringResource = when (error) {
        LogoutError.UNKNOWN -> Res.string.logout_error_message
    }
}
