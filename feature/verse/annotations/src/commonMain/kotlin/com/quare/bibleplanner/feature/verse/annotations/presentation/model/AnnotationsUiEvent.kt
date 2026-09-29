package com.quare.bibleplanner.feature.verse.annotations.presentation.model

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface AnnotationsUiEvent : UiEvent {
    data object OnBackClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnSearchClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_SEARCH_OPENED,
            params = emptyMap(),
        )
    }

    data class OnSearchQueryChange(
        val query: String,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnSearchClearClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_SEARCH_CLEARED,
            params = emptyMap(),
        )
    }

    data object OnSearchCloseClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_SEARCH_CLOSED,
            params = emptyMap(),
        )
    }

    data class OnTypeFilterClick(
        val type: AnnotationTypeFilter,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.ANNOTATIONS_TYPE_FILTER_CHANGED,
        )
    }

    data class OnColorFilterClick(
        val color: HighlightColor,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.ANNOTATIONS_COLOR_FILTER_TOGGLED,
        )
    }

    data object OnBookFilterClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_BOOK_FILTER_OPENED,
            params = emptyMap(),
        )
    }

    data class OnBookSelected(
        val bookId: BookId?,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.ANNOTATIONS_BOOK_FILTER_CHANGED,
        )
    }

    data object OnPeriodFilterClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_OPENED,
            params = emptyMap(),
        )
    }

    data class OnPeriodSelected(
        val period: AnnotationPeriod,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_CHANGED,
                AnalyticsEventNames.ANNOTATIONS_CUSTOM_RANGE_OPENED,
            ),
        )
    }

    data class OnCustomRangeApply(
        val startUtcMillis: Long,
        val endUtcMillis: Long?,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_CHANGED,
        )
    }

    data object OnCustomRangeDismiss : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_CUSTOM_RANGE_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnFilterMenuDismiss : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_FILTER_MENU_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnClearFiltersClick : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_FILTERS_CLEARED,
            params = emptyMap(),
        )
    }

    data class OnItemClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = item.toChapterOpenedAnalytics(SOURCE_ROW)
    }

    data class OnItemMenuClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_MENU_OPENED,
            params = emptyMap(),
        )
    }

    data object OnItemMenuDismiss : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_MENU_DISMISSED,
            params = emptyMap(),
        )
    }

    data class OnOpenInChapterClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = item.toChapterOpenedAnalytics(SOURCE_MENU)
    }

    data class OnNoteClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_NOTE_OPENED,
            params = mapOf(
                AnalyticsParams.IS_EXISTING to (item.passage.note != null),
                AnalyticsParams.VERSE_COUNT to item.passage.verseNumbers.size,
            ),
        )
    }

    data class OnShareClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_SHARE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to item.passage.verseNumbers.size),
        )
    }

    data class OnRemoveClick(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_REMOVAL_OPENED,
            params = emptyMap(),
        )
    }

    data class OnRemoveConfirm(
        val item: AnnotationItemUiModel,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_REMOVAL_CONFIRMED,
            params = mapOf(
                AnalyticsParams.VERSE_COUNT to item.passage.verseNumbers.size,
                AnalyticsParams.HAS_HIGHLIGHT to (item.passage.highlightColor != null),
                AnalyticsParams.IS_SAVED to item.passage.isSaved,
                AnalyticsParams.HAS_NOTE to (item.passage.note != null),
            ),
        )
    }

    data class OnUseVersionClick(
        val bibleVersionId: String,
        val isEmptyState: Boolean,
    ) : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATIONS_OTHER_VERSION_USED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to bibleVersionId,
                AnalyticsParams.SOURCE to if (isEmptyState) SOURCE_EMPTY_STATE else SOURCE_LIST,
            ),
        )
    }

    data object OnRemoveCancel : AnnotationsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.ANNOTATION_REMOVAL_CANCELLED,
            params = emptyMap(),
        )
    }

    private companion object {
        const val SOURCE_ROW = "row"
        const val SOURCE_MENU = "menu"
        const val SOURCE_EMPTY_STATE = "empty_state"
        const val SOURCE_LIST = "list"

        fun AnnotationItemUiModel.toChapterOpenedAnalytics(source: String): EventAnalytics =
            EventAnalytics.Track.Automatic(
                name = AnalyticsEventNames.ANNOTATION_CHAPTER_OPENED,
                params = mapOf(
                    AnalyticsParams.SOURCE to source,
                    AnalyticsParams.BOOK_ID to passage.chapter.bookId.name
                        .lowercase(),
                    AnalyticsParams.CHAPTER_NUMBER to passage.chapter.chapterNumber,
                ),
            )
    }
}
