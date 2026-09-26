package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase

fun interface SetStudySuggestionSyncEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
