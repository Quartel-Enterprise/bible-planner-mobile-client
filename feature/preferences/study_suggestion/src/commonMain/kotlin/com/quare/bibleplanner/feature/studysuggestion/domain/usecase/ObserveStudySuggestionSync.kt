package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

fun interface ObserveStudySuggestionSync {
    suspend operator fun invoke()
}
