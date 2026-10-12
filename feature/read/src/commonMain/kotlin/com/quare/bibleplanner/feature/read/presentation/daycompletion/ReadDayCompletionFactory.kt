package com.quare.bibleplanner.feature.read.presentation.daycompletion

import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.plan.domain.usecase.GetCompletedDayForChapter
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSettings
import kotlinx.coroutines.CoroutineScope

internal class ReadDayCompletionFactory(
    private val navigator: Navigator,
    private val getCompletedDayForChapter: GetCompletedDayForChapter,
    private val observeStudySuggestionSettings: ObserveStudySuggestionSettings,
) {
    fun create(scope: CoroutineScope): ReadDayCompletion = ReadDayCompletion(
        navigator = navigator,
        getCompletedDayForChapter = getCompletedDayForChapter,
        scope = scope,
        observeStudySuggestionSettings = observeStudySuggestionSettings,
    )
}
