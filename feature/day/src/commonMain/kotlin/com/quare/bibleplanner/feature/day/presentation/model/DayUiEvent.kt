package com.quare.bibleplanner.feature.day.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.feature.day.domain.model.ChapterClickStrategy
import com.quare.bibleplanner.feature.day.domain.model.UpdateReadStatusOfPassageStrategy
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface DayUiEvent : UiEvent {
    data class OnChapterCheckboxClick(
        val strategy: UpdateReadStatusOfPassageStrategy,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(AnalyticsEventNames.CHAPTER_READ_TOGGLED, AnalyticsEventNames.BOOK_READ_TOGGLED),
        )
    }

    data class OnChapterClick(
        val strategy: ChapterClickStrategy,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_CLICKED,
            params = mapOf(AnalyticsParams.SOURCE to "day_screen"),
        )
    }

    data object OnDayReadToggle : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.DAY_READ_TOGGLED,
        )
    }

    data class OnEditReadDate(
        val hour: Int,
        val minute: Int,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.READ_DATE_EDITED,
        )
    }

    data object OnEditDateClick : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_EDIT_DATE_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnShowTimePicker : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_TIME_PICKER_SHOWN,
            params = emptyMap(),
        )
    }

    data object OnDismissPicker : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_DATE_PICKER_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnDateSelected(
        val utcDateMillis: Long,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_DATE_SELECTED,
            params = emptyMap(),
        )
    }

    data class OnNotesChanged(
        val notes: String,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.NOTE_SAVED,
        )
    }

    data object OnNotesClear : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.NOTES_CLEAR_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnNotesFocus : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.NOTES_LIMIT_REACHED,
        )
    }

    data object OnBackClick : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnDayStudySubscribeClick : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.PAYWALL_VIEWED,
            params = mapOf(AnalyticsParams.SOURCE to SOURCE_DAY_STUDY),
        )
    }

    data object OnDayStudyLoginRequired : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.DAY_STUDY_LOGIN_REQUIRED_CLICKED,
            params = emptyMap(),
        )
    }

    data class OnDayStudyMessage(
        val message: String,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnDayStudyNavigate : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnWidthClassChanged(
        val isWide: Boolean,
    ) : DayUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    companion object {
        private const val SOURCE_DAY_STUDY = "day_study"
    }
}
