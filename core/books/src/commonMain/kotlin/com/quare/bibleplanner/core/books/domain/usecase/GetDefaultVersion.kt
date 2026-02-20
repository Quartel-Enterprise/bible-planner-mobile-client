package com.quare.bibleplanner.core.books.domain.usecase

import com.quare.bibleplanner.core.utils.locale.AppLanguage
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage

fun getDefaultVersion(): String = if (getCurrentLanguage() == AppLanguage.PORTUGUESE_BRAZIL) {
    "ACF"
} else {
    "WEB"
}
