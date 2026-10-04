package com.quare.bibleplanner.core.model.loginwarning

/*
 * Why: key is the stable id that carries the reason through type-safe navigation,
 * so its values must not change.
 */
sealed interface LoginWarningReason {
    val key: String

    data object Purchase : LoginWarningReason {
        override val key: String = "purchase"
    }

    data object DayStudy : LoginWarningReason {
        override val key: String = "day_study"
    }

    data object ChapterStudy : LoginWarningReason {
        override val key: String = "chapter_study"
    }

    data object AiChat : LoginWarningReason {
        override val key: String = "ai_chat"
    }

    sealed interface Preferences : LoginWarningReason {
        data object Theme : Preferences {
            override val key: String = "preferences_theme"
        }

        data object Language : Preferences {
            override val key: String = "preferences_language"
        }

        data object StudySuggestion : Preferences {
            override val key: String = "preferences_study_suggestion"
        }
    }

    companion object {
        fun fromKey(key: String): LoginWarningReason = listOf(
            Purchase,
            DayStudy,
            ChapterStudy,
            AiChat,
            Preferences.Theme,
            Preferences.Language,
            Preferences.StudySuggestion,
        ).first { it.key == key }
    }
}
