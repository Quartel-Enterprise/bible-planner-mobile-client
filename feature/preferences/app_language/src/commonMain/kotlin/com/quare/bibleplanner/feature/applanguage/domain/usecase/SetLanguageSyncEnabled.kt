package com.quare.bibleplanner.feature.applanguage.domain.usecase

fun interface SetLanguageSyncEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
