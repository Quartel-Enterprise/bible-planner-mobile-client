package com.quare.bibleplanner.feature.studysuggestion.domain.usecase

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode

fun interface SetStudySuggestionMode {
    suspend operator fun invoke(mode: StudySuggestionMode)
}
