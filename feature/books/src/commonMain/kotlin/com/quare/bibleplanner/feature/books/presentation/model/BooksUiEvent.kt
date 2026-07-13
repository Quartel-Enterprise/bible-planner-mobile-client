package com.quare.bibleplanner.feature.books.presentation.model

import com.quare.bibleplanner.core.books.presentation.model.BookTestament
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface BooksUiEvent : UiEvent {
    data class OnSearchQueryChange(
        val query: String,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOKS_SEARCH_USED,
        )
    }

    data class OnTestamentSelect(
        val testament: BookTestament,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.TESTAMENT_SWITCHED,
            params = mapOf(AnalyticsParams.TESTAMENT to testament.toAnalyticsValue()),
        )
    }

    data class OnBookClick(
        val book: BookPresentationModel,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOK_CLICKED,
            params = mapOf(AnalyticsParams.BOOK_ID to book.id.name.lowercase()),
        )
    }

    data class OnToggleFilter(
        val filterType: BookFilterType,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOKS_FILTER_TOGGLED,
        )
    }

    data class OnToggleFavorite(
        val bookId: BookId,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOK_FAVORITE_TOGGLED,
        )
    }

    data object OnToggleFilterMenu : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_FILTER_MENU_OPENED,
            params = emptyMap(),
        )
    }

    data object OnToggleSortMenu : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_SORT_MENU_OPENED,
            params = emptyMap(),
        )
    }

    data class OnSortOrderSelect(
        val sortOrder: BookSortOrder,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            AnalyticsEventNames.BOOKS_SORT_CHANGED,
        )
    }

    data object OnDismissSortMenu : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_SORT_MENU_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnDismissFilterMenu : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_FILTER_MENU_DISMISSED,
            params = emptyMap(),
        )
    }

    data object OnClearSearch : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_SEARCH_CLEARED,
            params = emptyMap(),
        )
    }

    data class OnLayoutFormatSelect(
        val layoutFormat: BookLayoutFormat,
    ) : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.BOOKS_LAYOUT_CHANGED,
            params = mapOf(AnalyticsParams.LAYOUT to layoutFormat.toAnalyticsValue()),
        )
    }

    data object OnWebAppLinkClick : BooksUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.WEB_APP_LINK_OPENED,
            params = emptyMap(),
        )
    }
}
