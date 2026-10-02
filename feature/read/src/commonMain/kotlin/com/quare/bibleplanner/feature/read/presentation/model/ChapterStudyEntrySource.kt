package com.quare.bibleplanner.feature.read.presentation.model

enum class ChapterStudyEntrySource {
    TOP_BAR,
    CHAPTER_END,
    ;

    val key: String
        get() = name.lowercase()
}
