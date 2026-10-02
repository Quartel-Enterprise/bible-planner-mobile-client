package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class ChatEntrySource {
    DAY_FAB,
    DAY_STUDY_QUESTIONS,
    CHAPTER_STUDY,
    ;

    val key: String
        get() = name.lowercase()
}
