package com.quare.bibleplanner.core.preferences.themeselection.domain.usecase

fun interface SetThemeSyncEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
