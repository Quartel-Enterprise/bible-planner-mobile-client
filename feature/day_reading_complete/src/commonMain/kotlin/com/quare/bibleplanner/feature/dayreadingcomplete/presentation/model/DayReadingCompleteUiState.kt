package com.quare.bibleplanner.feature.dayreadingcomplete.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.DayTimingState
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import kotlinx.datetime.LocalDate

sealed interface DayReadingCompleteUiState {
    data object Loading : DayReadingCompleteUiState

    data class Loaded(
        val timing: DayTimingState,
        val plannedReadDate: LocalDate?,
        val passages: List<PassageModel>,
        val chapterCount: Int,
        val ctaState: Loadable<StudyCtaState>,
        val language: Language,
    ) : DayReadingCompleteUiState
}
