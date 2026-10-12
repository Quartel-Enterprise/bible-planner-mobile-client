package com.quare.bibleplanner.feature.read.presentation.daycompletion

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetCompletedDayForChapter
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class ReadDayCompletion(
    private val navigator: Navigator,
    private val getCompletedDayForChapter: GetCompletedDayForChapter,
    scope: CoroutineScope,
    observeStudySuggestionSettings: ObserveStudySuggestionSettings,
) {
    val banner: StateFlow<PlanDayLocationModel?>
        field = MutableStateFlow<PlanDayLocationModel?>(null)

    /*
     * Why: SharingStarted.Eagerly keeps the setting warm so the tap that ends a day never
     * waits on the preference.
     */
    private val studySuggestionSettings: StateFlow<StudySuggestionSettingsModel?> =
        observeStudySuggestionSettings()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    /*
     * Why: safety net for a tap that beat the day completion candidates (cold screen, off-screen
     * chapter). The reader never knows which plan day opened it, so the day is looked up
     * from the chapter.
     */
    suspend fun checkChapter(
        bookId: BookId,
        chapterNumber: Int,
    ) {
        val day = getCompletedDayForChapter(
            bookId = bookId,
            chapterNumber = chapterNumber,
        ) ?: return
        present(day)
    }

    /*
     * Why: settings not yet loaded fall back to the sheet rather than swallowing a
     * celebration the reader just earned.
     */
    fun present(day: PlanDayLocationModel) {
        val settings = studySuggestionSettings.value
        when {
            settings == null -> navigator.navigate(day.toDayReadingCompleteRoute())
            !settings.isEnabled -> Unit
            settings.mode == StudySuggestionMode.BANNER -> banner.update { day }
            else -> navigator.navigate(day.toDayReadingCompleteRoute())
        }
    }

    fun dismissBanner() {
        banner.update { null }
    }

    private fun PlanDayLocationModel.toDayReadingCompleteRoute(): DayReadingCompleteNavRoute =
        DayReadingCompleteNavRoute(
            dayNumber = dayNumber,
            weekNumber = weekNumber,
            readingPlanType = readingPlanType.name,
        )
}
