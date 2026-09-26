package com.quare.bibleplanner.ui.component.language

import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.language_english
import bibleplanner.ui.component.generated.resources.language_portuguese_brazil
import bibleplanner.ui.component.generated.resources.language_spanish
import com.quare.bibleplanner.core.utils.locale.Language
import org.jetbrains.compose.resources.StringResource

fun Language.toStringResource(): StringResource = when (this) {
    Language.ENGLISH -> Res.string.language_english
    Language.PORTUGUESE_BRAZIL -> Res.string.language_portuguese_brazil
    Language.SPANISH -> Res.string.language_spanish
}
