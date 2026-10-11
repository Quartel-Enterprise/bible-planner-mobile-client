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
    NARRATION_PAUSED,
    NEXT_LOCKED,
    FINISHED,
    ;

    // Why: the chapter couldn't be loaded, so the UI explains why and playing again reloads it.
    val isUnavailable: Boolean
        get() = this == NEEDS_CONNECTION ||
            this == NEEDS_SIGN_IN ||
            this == VERSION_UNAVAILABLE ||
            this == AUDIO_UNAVAILABLE ||
            this == NARRATION_PAUSED
}
