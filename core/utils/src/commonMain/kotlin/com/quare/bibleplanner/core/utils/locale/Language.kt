package com.quare.bibleplanner.core.utils.locale

enum class Language {
    ENGLISH,
    PORTUGUESE_BRAZIL,
    SPANISH,
}

val Language.isPortugueseBrazil: Boolean get() = this == Language.PORTUGUESE_BRAZIL
