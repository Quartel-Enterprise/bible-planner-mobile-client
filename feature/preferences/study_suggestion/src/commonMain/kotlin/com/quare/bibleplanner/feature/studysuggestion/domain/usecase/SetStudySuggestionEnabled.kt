package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

fun interface SetStudySuggestionEnabled {
    suspend operator fun invoke(isEnabled: Boolean)
}
