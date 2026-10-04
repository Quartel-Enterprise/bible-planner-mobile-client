package com.quare.bibleplanner.core.chapterlistening.domain.model

enum class ListeningModeModel {
    CHAPTER,
    DAY_READING,
    ;

    val key: String
        get() = name.lowercase()
}
