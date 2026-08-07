package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

/** Which surface opened the AI chat. Reported as the `source` analytics parameter. */
@Serializable
enum class ChatEntrySource {
    DAY_FAB,
    DAY_STUDY_QUESTIONS,
    ;

    val key: String
        get() = name.lowercase()
}
