package com.quare.bibleplanner.core.model.loginwarning

/**
 * Why the login-warning dialog is being shown: a logged-out user tried to enable a setting that needs
 * an account to persist and sync. Grouped by area (e.g. [Preferences]) so new cases slot in without a
 * flat list of reasons. [key] is the stable identifier used to carry the reason through type-safe
 * navigation.
 */
sealed interface LoginWarningReason {
    val key: String

    data object Purchase : LoginWarningReason {
        override val key: String = "purchase"
    }

    data object DayStudy : LoginWarningReason {
        override val key: String = "day_study"
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
        private val entries: List<LoginWarningReason> = listOf(
            Purchase,
            DayStudy,
            AiChat,
            Preferences.Theme,
            Preferences.Language,
            Preferences.StudySuggestion,
        )

        fun fromKey(key: String): LoginWarningReason = entries.first { it.key == key }
    }
}
