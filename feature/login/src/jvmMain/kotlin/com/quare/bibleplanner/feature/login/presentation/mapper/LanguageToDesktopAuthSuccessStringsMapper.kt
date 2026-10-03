package com.quare.bibleplanner.feature.login.presentation.mapper

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.model.DesktopAuthSuccessStrings

internal class LanguageToDesktopAuthSuccessStringsMapper {
    fun map(language: Language): DesktopAuthSuccessStrings = when (language) {
        Language.PORTUGUESE_BRAZIL -> DesktopAuthSuccessStrings(
            heading = "Login concluído!",
            message = "Você pode fechar esta aba e voltar para o Bible Planner.",
            htmlLang = "pt-BR",
        )

        Language.SPANISH -> DesktopAuthSuccessStrings(
            heading = "¡Sesión iniciada!",
            message = "Puedes cerrar esta pestaña y volver a Bible Planner.",
            htmlLang = "es",
        )

        Language.ENGLISH -> DesktopAuthSuccessStrings(
            heading = "Signed in!",
            message = "You can close this tab and return to Bible Planner.",
            htmlLang = "en",
        )
    }
}
