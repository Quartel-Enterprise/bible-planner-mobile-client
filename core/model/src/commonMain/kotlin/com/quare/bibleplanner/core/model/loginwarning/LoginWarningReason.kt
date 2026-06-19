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

    sealed interface Preferences : LoginWarningReason {
        data object Theme : Preferences {
            override val key: String = "preferences_theme"
        }

        data object Language : Preferences {
            override val key: String = "preferences_language"
        }
    }

    companion object {
        private val entries: List<LoginWarningReason> = listOf(
            Purchase,
            Preferences.Theme,
            Preferences.Language,
        )

        fun fromKey(key: String): LoginWarningReason = entries.first { it.key == key }
    }
}
