package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode

fun interface SetStudySuggestionMode {
    suspend operator fun invoke(mode: StudySuggestionMode)
}
