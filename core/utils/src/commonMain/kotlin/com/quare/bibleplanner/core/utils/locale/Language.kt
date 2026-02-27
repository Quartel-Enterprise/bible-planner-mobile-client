package com.quare.bibleplanner.core.utils.locale

enum class Language {
    ENGLISH,
    PORTUGUESE_BRAZIL,
    SPANISH,
}

expect fun getCurrentLanguage(): Language
