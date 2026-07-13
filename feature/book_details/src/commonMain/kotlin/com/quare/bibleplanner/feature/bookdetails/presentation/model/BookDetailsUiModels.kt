package com.quare.bibleplanner.feature.bookdetails.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent
import org.jetbrains.compose.resources.StringResource

sealed interface BookDetailsUiState {
    data object Loading : BookDetailsUiState

    data class Success(
        val id: BookId,
        val nameStringResource: StringResource,
        val synopsisStringResource: StringResource,
        val chapters: List<BookChapterModel>,
        val progress: Float,
        val readChaptersCount: Int,
        val totalChaptersCount: Int,
        val areAllChaptersRead: Boolean,
        val isFavorite: Boolean,
        val bookGroup: BookGroup,
        val bookCategoryName: String,
        val isSynopsisExpanded: Boolean = false,
    ) : BookDetailsUiState
}

sealed interface BookDetailsUiEvent : UiEvent {
    data object OnBackClick : BookDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOK_DETAILS_BACK_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnToggleFavorite : BookDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOK_FAVORITE_TOGGLED,
        )
    }

    data object OnToggleSynopsisExpanded : BookDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.SYNOPSIS_TOGGLED,
        )
    }

    data object OnToggleAllChapters : BookDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOK_READ_TOGGLED,
        )
    }

    data class OnChapterClick(
        val chapterNumber: Int,
    ) : BookDetailsUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.CHAPTER_CLICKED,
            params = mapOf(
                AnalyticsParams.CHAPTER_NUMBER to chapterNumber,
                AnalyticsParams.SOURCE to "book_details",
            ),
        )
    }
}
