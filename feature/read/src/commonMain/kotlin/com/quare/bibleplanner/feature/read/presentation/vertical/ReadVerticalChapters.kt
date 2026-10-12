package com.quare.bibleplanner.feature.read.presentation.vertical

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetPreviousChapter
import com.quare.bibleplanner.feature.read.presentation.model.VerticalChapterCounts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReadVerticalChapters(
    private val scope: CoroutineScope,
    private val bookId: BookId,
    private val chapterNumber: Int,
    private val shouldForceCanonOrder: Boolean,
    private val getNextChapter: GetNextChapter,
    private val getPreviousChapter: GetPreviousChapter,
) {
    private val isAppendRequested = MutableStateFlow(false)
    private val isPrependRequested = MutableStateFlow(false)
    private val nextChapter = MutableStateFlow<Loadable<ReadNavigationSuggestionModel?>>(Loadable.Loading)
    private val previousChapter = MutableStateFlow<Loadable<ReadNavigationSuggestionModel?>>(Loadable.Loading)

    val appended: StateFlow<List<ReadNavigationSuggestionModel>>
        field = MutableStateFlow<List<ReadNavigationSuggestionModel>>(emptyList())

    val prepended: StateFlow<List<ReadNavigationSuggestionModel>>
        field = MutableStateFlow<List<ReadNavigationSuggestionModel>>(emptyList())

    val requestedCounts: Flow<VerticalChapterCounts> = combine(
        prepended,
        appended,
        isPrependRequested,
        isAppendRequested,
    ) { prepended, appended, isPrependPending, isAppendPending ->
        VerticalChapterCounts(
            prepended = prepended.size + if (isPrependPending) 1 else 0,
            appended = appended.size + if (isAppendPending) 1 else 0,
        )
    }

    suspend fun enable() {
        resolveNextChapter()
        resolvePreviousChapter()
    }

    fun disable() {
        appended.update { emptyList() }
        prepended.update { emptyList() }
        isAppendRequested.update { false }
        isPrependRequested.update { false }
        nextChapter.update { Loadable.Loading }
        previousChapter.update { Loadable.Loading }
    }

    /*
     * Why: the request stands until the reading order resolves, so an end reached before the
     * lookup settles is served then instead of stalling until the next scroll.
     */
    fun appendNextChapter() {
        isAppendRequested.update { true }
        consumeNextChapter()
    }

    fun prependPreviousChapter() {
        isPrependRequested.update { true }
        consumePreviousChapter()
    }

    private fun consumeNextChapter() {
        val resolvedNextChapter = nextChapter.value
        if (resolvedNextChapter !is Loadable.Loaded || !isAppendRequested.value) return
        isAppendRequested.update { false }
        val chapter = resolvedNextChapter.value ?: return
        nextChapter.update { Loadable.Loading }
        appended.update { it + chapter }
        scope.launch { resolveNextChapter() }
    }

    private suspend fun resolveNextChapter() {
        val lastChapter = appended.value.lastOrNull()
        nextChapter.update { Loadable.Loading }
        val next = getNextChapter(
            bookId = lastChapter?.bookId ?: bookId,
            chapterNumber = lastChapter?.chapterNumber ?: chapterNumber,
            shouldForceCanonOrder = shouldForceCanonOrder,
        )
        nextChapter.update { Loadable.Loaded(next) }
        consumeNextChapter()
    }

    private fun consumePreviousChapter() {
        val resolvedPreviousChapter = previousChapter.value
        if (resolvedPreviousChapter !is Loadable.Loaded || !isPrependRequested.value) return
        isPrependRequested.update { false }
        val chapter = resolvedPreviousChapter.value ?: return
        previousChapter.update { Loadable.Loading }
        prepended.update { listOf(chapter) + it }
        scope.launch { resolvePreviousChapter() }
    }

    private suspend fun resolvePreviousChapter() {
        val firstChapter = prepended.value.firstOrNull()
        previousChapter.update { Loadable.Loading }
        val previous = getPreviousChapter(
            bookId = firstChapter?.bookId ?: bookId,
            chapterNumber = firstChapter?.chapterNumber ?: chapterNumber,
            shouldForceCanonOrder = shouldForceCanonOrder,
        )
        previousChapter.update { Loadable.Loaded(previous) }
        consumePreviousChapter()
    }
}
