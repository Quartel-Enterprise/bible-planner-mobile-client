package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase

fun interface ObserveStudySuggestionSync {
    suspend operator fun invoke()
}
