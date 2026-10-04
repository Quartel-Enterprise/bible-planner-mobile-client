package com.quare.bibleplanner.feature.read.presentation.listening.model

enum class ListeningEntrySource {
    BOTTOM_BAR,
    HEADER,
    SHORTCUT,
    ;

    val key: String
        get() = name.lowercase()
}
