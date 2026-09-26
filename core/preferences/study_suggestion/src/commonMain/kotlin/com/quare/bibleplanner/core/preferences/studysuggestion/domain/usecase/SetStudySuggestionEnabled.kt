package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase

fun interface SetStudySuggestionEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
