package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

fun interface SetStudySuggestionSyncEnabled {
    suspend operator fun invoke(enabled: Boolean)
}
