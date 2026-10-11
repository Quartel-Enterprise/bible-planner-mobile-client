package com.quare.bibleplanner.core.chapterlistening.domain.model

enum class ListeningStatusModel {
    PREPARING,
    PLAYING,
    PAUSED,
    INTERRUPTED,
    NEEDS_CONNECTION,
    NEEDS_SIGN_IN,
    VERSION_UNAVAILABLE,
    AUDIO_UNAVAILABLE,
    NEXT_LOCKED,
    FINISHED,
}
