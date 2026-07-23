package com.quare.bibleplanner.feature.daystudy.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DayStudyBackgroundGenerationUiEvent : UiEvent {
    data class OnOpenClick(
        val job: DayStudyGenerationJob,
    ) : DayStudyBackgroundGenerationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_STUDY_BG_CARD_OPENED,
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to job.dayRoute.readingPlanType,
                AnalyticsParams.WEEK_NUMBER to job.dayRoute.weekNumber,
                AnalyticsParams.DAY_NUMBER to job.dayRoute.dayNumber,
                AnalyticsParams.IS_READY to (job.status is DayStudyGenerationStatus.Done),
            ),
        )
    }

    data class OnDismissClick(
        val keys: List<String>,
    ) : DayStudyBackgroundGenerationUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_STUDY_BG_CARD_DISMISSED,
            params = mapOf(
                AnalyticsParams.COUNT to keys.size,
            ),
        )
    }
}
