package com.quare.bibleplanner.feature.chapterstudy.presentation.model

import com.quare.bibleplanner.core.chapterstudy.domain.model.CrossReferenceModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.OutlineSectionModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

internal sealed interface ChapterStudyUiEvent : UiEvent {
    data object OnRetryClick : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.CHAPTER_STUDY_RETRY_CLICKED,
                AnalyticsEventNames.CHAPTER_STUDY_GENERATION_STARTED,
                AnalyticsEventNames.CHAPTER_STUDY_GENERATION_FAILED,
            ),
        )
    }

    data class OnOutlineSectionClick(
        val section: OutlineSectionModel,
    ) : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_STUDY_OUTLINE_CLICKED,
        )
    }

    data object OnShareKeyVerseClick : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.CHAPTER_STUDY_KEY_VERSE_SHARE_CLICKED,
        )
    }

    data class OnCrossReferenceClick(
        val reference: CrossReferenceModel,
    ) : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_STUDY_CROSS_REFERENCE_CLICKED,
            params = mapOf(
                AnalyticsParams.BOOK_ID to reference.bookId.name,
                AnalyticsParams.CHAPTER_NUMBER to reference.chapterNumber,
            ),
        )
    }

    data object OnAskAiClick : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.AI_CHAT_ENTRY_CLICKED,
        )
    }

    data class OnWidthClassChanged(
        val isWide: Boolean,
    ) : ChapterStudyUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }
}
