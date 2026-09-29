package com.quare.bibleplanner.feature.verse.annotations.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.verse.annotations.generated.resources.Res
import bibleplanner.feature.verse.annotations.generated.resources.annotation_removed
import com.quare.bibleplanner.core.books.domain.usecase.IsWholeChapterRead
import com.quare.bibleplanner.core.books.domain.usecase.SetSelectedVersion
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.usecase.RemovePassageAnnotations
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveAnnotationEntries
import com.quare.bibleplanner.feature.verse.annotations.domain.usecase.ObserveOtherVersionAnnotationCounts
import com.quare.bibleplanner.feature.verse.annotations.presentation.factory.AnnotationsContentFactory
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationDateRange
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationFilterMenu
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationItemUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationPeriod
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationTypeFilter
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsContentUiModel
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsOverlayState
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiAction
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiEvent
import com.quare.bibleplanner.feature.verse.annotations.presentation.model.AnnotationsUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal class AnnotationsViewModel(
    private val removePassageAnnotations: RemovePassageAnnotations,
    private val isWholeChapterRead: IsWholeChapterRead,
    private val setSelectedVersion: SetSelectedVersion,
    private val navigator: Navigator,
    val platform: Platform,
    contentFactory: AnnotationsContentFactory,
    observeAnnotationEntries: ObserveAnnotationEntries,
    observeOtherVersionAnnotationCounts: ObserveOtherVersionAnnotationCounts,
    trackEvent: TrackEvent,
) : TrackedViewModel<AnnotationsUiEvent>(trackEvent) {
    private val filters = MutableStateFlow(AnnotationsContentFactory.noFilters)
    private val overlay = MutableStateFlow(
        AnnotationsOverlayState(
            openMenuItemKey = null,
            openFilterMenu = null,
            pendingRemoval = null,
            isCustomRangePickerOpen = false,
            isSearchOpen = false,
        ),
    )

    val uiAction: SharedFlow<AnnotationsUiAction>
        field = MutableSharedFlow<AnnotationsUiAction>()

    val uiState: StateFlow<AnnotationsUiState> = combine(
        combine(
            observeAnnotationEntries(),
            observeOtherVersionAnnotationCounts(),
            filters,
            contentFactory::create,
        ).map<AnnotationsContentUiModel, Loadable<AnnotationsContentUiModel>> { content -> Loadable.Loaded(content) }
            .onStart { emit(Loadable.Loading) },
        overlay,
        filters,
    ) { content, overlayState, currentFilters ->
        AnnotationsUiState(
            content = content,
            openMenuItemKey = overlayState.openMenuItemKey,
            openFilterMenu = overlayState.openFilterMenu,
            pendingRemoval = overlayState.pendingRemoval,
            isCustomRangePickerOpen = overlayState.isCustomRangePickerOpen,
            isSearchOpen = overlayState.isSearchOpen,
            searchQuery = currentFilters.query,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AnnotationsUiState(
            content = Loadable.Loading,
            openMenuItemKey = null,
            openFilterMenu = null,
            pendingRemoval = null,
            isCustomRangePickerOpen = false,
            isSearchOpen = false,
            searchQuery = "",
        ),
    )

    override fun handleEvent(event: AnnotationsUiEvent) {
        when (event) {
            AnnotationsUiEvent.OnBackClick -> navigator.navigateBack()

            AnnotationsUiEvent.OnSearchClick -> overlay.update {
                it.copy(
                    isSearchOpen = true,
                    openMenuItemKey = null,
                )
            }

            is AnnotationsUiEvent.OnSearchQueryChange -> filters.update { it.copy(query = event.query) }

            AnnotationsUiEvent.OnSearchClearClick -> filters.update { it.copy(query = "") }

            AnnotationsUiEvent.OnSearchCloseClick -> closeSearch()

            is AnnotationsUiEvent.OnTypeFilterClick -> toggleTypeFilter(event.type)

            is AnnotationsUiEvent.OnColorFilterClick -> toggleColorFilter(event.color)

            AnnotationsUiEvent.OnBookFilterClick -> openFilterMenu(AnnotationFilterMenu.BOOK)

            is AnnotationsUiEvent.OnBookSelected -> selectBook(event.bookId)

            AnnotationsUiEvent.OnPeriodFilterClick -> openFilterMenu(AnnotationFilterMenu.PERIOD)

            is AnnotationsUiEvent.OnPeriodSelected -> selectPeriod(event.period)

            is AnnotationsUiEvent.OnCustomRangeApply -> applyCustomRange(
                startUtcMillis = event.startUtcMillis,
                endUtcMillis = event.endUtcMillis,
            )

            AnnotationsUiEvent.OnCustomRangeDismiss -> overlay.update { it.copy(isCustomRangePickerOpen = false) }

            AnnotationsUiEvent.OnFilterMenuDismiss -> overlay.update { it.copy(openFilterMenu = null) }

            AnnotationsUiEvent.OnClearFiltersClick -> filters.update { AnnotationsContentFactory.noFilters }

            is AnnotationsUiEvent.OnItemClick -> openInChapter(event.item)

            is AnnotationsUiEvent.OnItemMenuClick -> overlay.update { it.copy(openMenuItemKey = event.item.key) }

            AnnotationsUiEvent.OnItemMenuDismiss -> closeItemMenu()

            is AnnotationsUiEvent.OnOpenInChapterClick -> openInChapter(event.item)

            is AnnotationsUiEvent.OnNoteClick -> openNote(event.item)

            is AnnotationsUiEvent.OnShareClick -> share(event.item)

            is AnnotationsUiEvent.OnRemoveClick -> overlay.update {
                it.copy(
                    openMenuItemKey = null,
                    pendingRemoval = event.item,
                )
            }

            is AnnotationsUiEvent.OnRemoveConfirm -> remove(event.item)

            AnnotationsUiEvent.OnRemoveCancel -> overlay.update { it.copy(pendingRemoval = null) }

            is AnnotationsUiEvent.OnUseVersionClick -> viewModelScope.launch {
                setSelectedVersion(event.bibleVersionId)
            }
        }
    }

    private fun toggleColorFilter(color: HighlightColor) {
        val isSelected = filters.value.color?.key != color.key
        filters.update { it.copy(color = color.takeIf { isSelected }) }
        trackEvent(
            name = AnalyticsEventNames.ANNOTATIONS_COLOR_FILTER_TOGGLED,
            params = mapOf(
                AnalyticsParams.COLOR to color.key,
                AnalyticsParams.IS_SELECTED to isSelected,
            ),
        )
    }

    private fun openFilterMenu(menu: AnnotationFilterMenu) {
        overlay.update { it.copy(openFilterMenu = menu) }
    }

    private fun closeSearch() {
        overlay.update { it.copy(isSearchOpen = false) }
        filters.update { it.copy(query = "") }
    }

    private fun toggleTypeFilter(type: AnnotationTypeFilter) {
        val newType = if (filters.value.type == type) AnnotationTypeFilter.ALL else type
        filters.update { it.copy(type = newType) }
        trackEvent(
            name = AnalyticsEventNames.ANNOTATIONS_TYPE_FILTER_CHANGED,
            params = mapOf(AnalyticsParams.FILTER_TYPE to newType.analyticsValue),
        )
    }

    private fun selectBook(bookId: BookId?) {
        val newBookId = bookId.takeUnless { it == filters.value.bookId }
        filters.update { it.copy(bookId = newBookId) }
        overlay.update { it.copy(openFilterMenu = null) }
        trackEvent(
            name = AnalyticsEventNames.ANNOTATIONS_BOOK_FILTER_CHANGED,
            params = mapOf(AnalyticsParams.BOOK_ID to (newBookId?.name?.lowercase() ?: ALL_BOOKS)),
        )
    }

    private fun selectPeriod(period: AnnotationPeriod) {
        val currentPeriod = filters.value.period
        if (period == AnnotationPeriod.CUSTOM && currentPeriod != AnnotationPeriod.CUSTOM) {
            overlay.update {
                it.copy(
                    openFilterMenu = null,
                    isCustomRangePickerOpen = true,
                )
            }
            trackEvent(
                name = AnalyticsEventNames.ANNOTATIONS_CUSTOM_RANGE_OPENED,
                params = emptyMap(),
            )
            return
        }
        val newPeriod = if (currentPeriod == period) AnnotationPeriod.ANY else period
        filters.update {
            it.copy(
                period = newPeriod,
                customRange = null,
            )
        }
        overlay.update { it.copy(openFilterMenu = null) }
        trackPeriodChanged(newPeriod)
    }

    private fun applyCustomRange(
        startUtcMillis: Long,
        endUtcMillis: Long?,
    ) {
        val start = startUtcMillis.toUtcDate()
        val end = endUtcMillis?.toUtcDate() ?: start
        filters.update {
            it.copy(
                period = AnnotationPeriod.CUSTOM,
                customRange = AnnotationDateRange(
                    start = minOf(start, end),
                    end = maxOf(start, end),
                ),
            )
        }
        overlay.update { it.copy(isCustomRangePickerOpen = false) }
        trackPeriodChanged(AnnotationPeriod.CUSTOM)
    }

    private fun trackPeriodChanged(period: AnnotationPeriod) {
        trackEvent(
            name = AnalyticsEventNames.ANNOTATIONS_PERIOD_FILTER_CHANGED,
            params = mapOf(AnalyticsParams.PERIOD to period.analyticsValue),
        )
    }

    private fun Long.toUtcDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

    private fun closeItemMenu() {
        overlay.update { it.copy(openMenuItemKey = null) }
    }

    private fun openInChapter(item: AnnotationItemUiModel) {
        closeItemMenu()
        val chapter = item.passage.chapter
        viewModelScope.launch {
            navigator.navigate(
                ReadNavRoute(
                    bookId = chapter.bookId.name,
                    chapterNumber = chapter.chapterNumber,
                    isChapterRead = isWholeChapterRead(
                        chapterNumber = chapter.chapterNumber,
                        bookId = chapter.bookId,
                    ),
                    isFromBookDetails = false,
                    targetVerseNumbers = item.passage.verseNumbers,
                ),
            )
        }
    }

    private fun openNote(item: AnnotationItemUiModel) {
        closeItemMenu()
        val passage = item.passage
        navigator.navigate(
            VerseNoteNavRoute(
                bibleVersionId = passage.chapter.bibleVersionId,
                bookId = passage.chapter.bookId.name,
                chapterNumber = passage.chapter.chapterNumber,
                verseNumbers = passage.note?.verseNumbers ?: passage.verseNumbers,
                noteId = passage.note?.id,
            ),
        )
    }

    private fun share(item: AnnotationItemUiModel) {
        closeItemMenu()
        val passage = item.passage
        navigator.navigate(
            ShareVerseNavRoute(
                bookId = passage.chapter.bookId.name,
                chapterNumber = passage.chapter.chapterNumber,
                verseNumbers = passage.verseNumbers,
            ),
        )
    }

    private fun remove(item: AnnotationItemUiModel) {
        overlay.update { it.copy(pendingRemoval = null) }
        viewModelScope.launch {
            removePassageAnnotations(item.passage)
            uiAction.emit(AnnotationsUiAction.ShowMessage(Res.string.annotation_removed))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val ALL_BOOKS = "all"
    }
}
