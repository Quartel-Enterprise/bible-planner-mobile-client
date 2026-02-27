package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage

fun getDefaultVersion(): String = if (getCurrentLanguage() == Language.PORTUGUESE_BRAZIL) {
    "ACF"
} else {
    "WEB"
}
