package com.quare.bibleplanner.core.provider.room.dao

/**
 * Keys for the cross-device settings stored in the synced key-value store ([SyncedPreferenceDao]).
 *
 * The `*_SYNC_ENABLED` flags are account-global by construction (they are ordinary synced rows). The
 * value keys ([APP_THEME], [THEME_CONTRAST], [DYNAMIC_COLORS_ENABLED], [APP_LANGUAGE]) are mirrored
 * into this store only while their flag is on; the device-local DataStore stays the render source, so
 * turning a flag off keeps each device's own value.
 */
object SyncedPreferenceKeys {
    const val THEME_SYNC_ENABLED = "theme_sync_enabled"
    const val APP_THEME = "app_theme"
    const val THEME_CONTRAST = "theme_contrast"
    const val DYNAMIC_COLORS_ENABLED = "dynamic_colors_enabled"

    const val LANGUAGE_SYNC_ENABLED = "language_sync_enabled"
    const val APP_LANGUAGE = "app_language"
}
