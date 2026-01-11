package com.quare.bibleplanner.core.utils.locale

enum class AppLanguage {
    ENGLISH,
    PORTUGUESE_BRAZIL,
    SPANISH,
}

expect fun getCurrentLanguage(): AppLanguage
