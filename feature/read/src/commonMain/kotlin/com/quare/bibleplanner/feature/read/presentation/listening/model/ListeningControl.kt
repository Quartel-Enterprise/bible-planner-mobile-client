package com.quare.bibleplanner.feature.read.presentation.listening.model

enum class ListeningControl {
    PLAY,
    PAUSE,
    RESUME,
    NEXT_VERSE,
    PREVIOUS_VERSE,
    NEXT_CHAPTER,
    PREVIOUS_CHAPTER,
    SEEK,
    ;

    val key: String
        get() = name.lowercase()
}
