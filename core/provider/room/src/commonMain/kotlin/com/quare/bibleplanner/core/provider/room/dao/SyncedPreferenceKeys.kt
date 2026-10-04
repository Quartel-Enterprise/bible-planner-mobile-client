package com.quare.bibleplanner.core.provider.room.dao

/*
 * Why: value keys are mirrored here only while their *_SYNC_ENABLED flag is on;
 * DataStore stays the render source, so turning a flag off keeps each device's value.
 */
object SyncedPreferenceKeys {
    const val THEME_SYNC_ENABLED = "theme_sync_enabled"
    const val APP_THEME = "app_theme"
    const val THEME_CONTRAST = "theme_contrast"
    const val DYNAMIC_COLORS_ENABLED = "dynamic_colors_enabled"

    const val LANGUAGE_SYNC_ENABLED = "language_sync_enabled"
    const val APP_LANGUAGE = "app_language"

    const val STUDY_SUGGESTION_SYNC_ENABLED = "study_suggestion_sync_enabled"
    const val STUDY_SUGGESTION_ENABLED = "study_suggestion_enabled"
    const val STUDY_SUGGESTION_MODE = "study_suggestion_mode"
}
