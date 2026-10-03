package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
enum class StudyUnlockSurface {
    CHAPTER_STUDY,
    DAY_STUDY,
    DAY_READING_COMPLETE,
    ;

    val key: String
        get() = name.lowercase()
}
