package com.quare.bibleplanner.feature.themeselection.domain.usecase

fun interface SetThemeSyncEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
