package com.quare.bibleplanner.feature.deletenotes.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.plan.domain.usecase.DeleteDayNotesUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.toAnalyticsValue
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.deletenotes.presentation.model.DeleteNotesUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.launch

internal class DeleteNotesViewModel(
    route: DeleteNotesRoute,
    private val deleteDayNotes: DeleteDayNotesUseCase,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteNotesUiEvent>(trackEvent) {
    private val readingPlanType = ReadingPlanType.valueOf(route.readingPlanType)
    private val weekNumber = route.week
    private val dayNumber = route.day

    override fun handleEvent(event: DeleteNotesUiEvent) {
        when (event) {
            DeleteNotesUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    deleteDayNotes(
                        weekNumber = weekNumber,
                        dayNumber = dayNumber,
                        readingPlanType = readingPlanType,
                    )
                    trackEvent(
                        name = AnalyticsEventNames.NOTE_DELETED,
                        params = mapOf(
                            AnalyticsParams.PLAN_TYPE to readingPlanType.toAnalyticsValue(),
                            AnalyticsParams.WEEK_NUMBER to weekNumber,
                            AnalyticsParams.DAY_NUMBER to dayNumber,
                        ),
                    )
                    navigator.navigateBack()
                }
            }

            DeleteNotesUiEvent.OnCancel -> {
                viewModelScope.launch {
                    navigator.navigateBack()
                }
            }
        }
    }
}
