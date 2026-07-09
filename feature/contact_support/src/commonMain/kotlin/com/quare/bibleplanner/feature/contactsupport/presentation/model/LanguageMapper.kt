package com.quare.bibleplanner.feature.contactsupport.presentation.model

import bibleplanner.feature.preferences.app_language.generated.resources.Res
import bibleplanner.feature.preferences.app_language.generated.resources.language_english
import bibleplanner.feature.preferences.app_language.generated.resources.language_portuguese_brazil
import bibleplanner.feature.preferences.app_language.generated.resources.language_spanish
import com.quare.bibleplanner.core.utils.locale.Language
import org.jetbrains.compose.resources.StringResource

internal fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> Res.string.language_english
    Language.PORTUGUESE_BRAZIL -> Res.string.language_portuguese_brazil
    Language.SPANISH -> Res.string.language_spanish
}
