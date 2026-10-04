package com.quare.bibleplanner.core.chapterlistening.domain.model

enum class ListeningSleepTimerOption {
    OFF,
    FIFTEEN_MINUTES,
    THIRTY_MINUTES,
    END_OF_CHAPTER,
    ;

    val key: String
        get() = name.lowercase()
}
