package com.quare.bibleplanner.feature.read.presentation.vertical

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetNextChapter
import com.quare.bibleplanner.feature.read.domain.usecase.GetPreviousChapter
import com.quare.bibleplanner.feature.read.presentation.model.VerticalChapterCounts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReadVerticalChaptersTest {
    private lateinit var verticalChapters: ReadVerticalChapters
    private lateinit var nextLookups: List<ChapterLocationModel>
    private lateinit var previousLookups: List<ChapterLocationModel>
    private lateinit var canonOrderFlags: List<Boolean>

    @Test
    fun `GIVEN an open chapter WHEN enabling THEN looks up both of its neighbours in the requested order`() = runTest {
        // Given
        prepareScenario(shouldForceCanonOrder = true)

        // When
        verticalChapters.enable()

        // Then
        assertEquals(
            expected = listOf(genesis(3)),
            actual = nextLookups,
        )
        assertEquals(
            expected = listOf(genesis(3)),
            actual = previousLookups,
        )
        assertEquals(
            expected = listOf(true, true),
            actual = canonOrderFlags,
        )
    }

    @Test
    fun `GIVEN the next chapter resolved WHEN appending it THEN appends it and looks up the one after`() = runTest {
        // Given
        prepareScenario()
        verticalChapters.enable()

        // When
        verticalChapters.appendNextChapter()
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(suggestion(4)),
            actual = verticalChapters.appended.value,
        )
        assertEquals(
            expected = listOf(genesis(3), genesis(4)),
            actual = nextLookups,
        )
    }

    @Test
    fun `GIVEN an appended chapter WHEN appending again THEN appends the chapter after it`() = runTest {
        // Given
        prepareScenario()
        verticalChapters.enable()
        verticalChapters.appendNextChapter()
        runCurrent()

        // When
        verticalChapters.appendNextChapter()
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(suggestion(4), suggestion(5)),
            actual = verticalChapters.appended.value,
        )
    }

    @Test
    fun `GIVEN an append requested before the lookup resolves WHEN the lookup resolves THEN serves the request`() =
        runTest {
            // Given
            val nextChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
            prepareScenario(getNextChapter = { _, _, _ -> nextChapter.await() })
            backgroundScope.launch { verticalChapters.enable() }
            runCurrent()
            verticalChapters.appendNextChapter()

            // When
            nextChapter.complete(suggestion(4))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(suggestion(4)),
                actual = verticalChapters.appended.value,
            )
        }

    @Test
    fun `GIVEN an append waiting on the lookup WHEN reading the requested counts THEN counts the pending chapter`() =
        runTest {
            // Given
            val nextChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
            prepareScenario(getNextChapter = { _, _, _ -> nextChapter.await() })
            backgroundScope.launch { verticalChapters.enable() }
            runCurrent()
            verticalChapters.appendNextChapter()

            // When
            val counts = verticalChapters.requestedCounts.first()

            // Then
            assertEquals(
                expected = VerticalChapterCounts(
                    prepended = 0,
                    appended = 1,
                ),
                actual = counts,
            )
        }

    @Test
    fun `GIVEN the end of the reading order WHEN appending THEN appends nothing and drops the request`() = runTest {
        // Given
        prepareScenario(getNextChapter = { _, _, _ -> null })
        verticalChapters.enable()

        // When
        verticalChapters.appendNextChapter()
        runCurrent()

        // Then
        assertTrue(verticalChapters.appended.value.isEmpty())
        assertEquals(
            expected = VerticalChapterCounts(
                prepended = 0,
                appended = 0,
            ),
            actual = verticalChapters.requestedCounts.first(),
        )
    }

    @Test
    fun `GIVEN the previous chapter resolved WHEN prepending it THEN prepends it and looks up the one before`() =
        runTest {
            // Given
            prepareScenario()
            verticalChapters.enable()

            // When
            verticalChapters.prependPreviousChapter()
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(suggestion(2)),
                actual = verticalChapters.prepended.value,
            )
            assertEquals(
                expected = listOf(genesis(3), genesis(2)),
                actual = previousLookups,
            )
        }

    @Test
    fun `GIVEN a prepended chapter WHEN prepending again THEN puts the earlier chapter first`() = runTest {
        // Given
        prepareScenario()
        verticalChapters.enable()
        verticalChapters.prependPreviousChapter()
        runCurrent()

        // When
        verticalChapters.prependPreviousChapter()
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(suggestion(1), suggestion(2)),
            actual = verticalChapters.prepended.value,
        )
    }

    @Test
    fun `GIVEN a prepend requested before the lookup resolves WHEN the lookup resolves THEN serves the request`() =
        runTest {
            // Given
            val previousChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
            prepareScenario(getPreviousChapter = { _, _, _ -> previousChapter.await() })
            backgroundScope.launch { verticalChapters.enable() }
            runCurrent()
            verticalChapters.prependPreviousChapter()

            // When
            previousChapter.complete(suggestion(2))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf(suggestion(2)),
                actual = verticalChapters.prepended.value,
            )
        }

    @Test
    fun `GIVEN a prepend waiting on the lookup WHEN reading the requested counts THEN counts the pending chapter`() =
        runTest {
            // Given
            val previousChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
            prepareScenario(getPreviousChapter = { _, _, _ -> previousChapter.await() })
            backgroundScope.launch { verticalChapters.enable() }
            runCurrent()
            verticalChapters.prependPreviousChapter()

            // When
            val counts = verticalChapters.requestedCounts.first()

            // Then
            assertEquals(
                expected = VerticalChapterCounts(
                    prepended = 1,
                    appended = 0,
                ),
                actual = counts,
            )
        }

    @Test
    fun `GIVEN the start of the reading order WHEN prepending THEN prepends nothing and drops the request`() = runTest {
        // Given
        prepareScenario(getPreviousChapter = { _, _, _ -> null })
        verticalChapters.enable()

        // When
        verticalChapters.prependPreviousChapter()
        runCurrent()

        // Then
        assertTrue(verticalChapters.prepended.value.isEmpty())
        assertEquals(
            expected = VerticalChapterCounts(
                prepended = 0,
                appended = 0,
            ),
            actual = verticalChapters.requestedCounts.first(),
        )
    }

    @Test
    fun `GIVEN appended and prepended chapters WHEN disabling THEN clears both sides`() = runTest {
        // Given
        prepareScenario()
        verticalChapters.enable()
        verticalChapters.appendNextChapter()
        verticalChapters.prependPreviousChapter()
        runCurrent()

        // When
        verticalChapters.disable()

        // Then
        assertTrue(verticalChapters.appended.value.isEmpty())
        assertTrue(verticalChapters.prepended.value.isEmpty())
    }

    @Test
    fun `GIVEN requests waiting on the lookups WHEN disabling THEN drops them`() = runTest {
        // Given
        val nextChapter = CompletableDeferred<ReadNavigationSuggestionModel?>()
        prepareScenario(getNextChapter = { _, _, _ -> nextChapter.await() })
        backgroundScope.launch { verticalChapters.enable() }
        runCurrent()
        verticalChapters.appendNextChapter()
        verticalChapters.prependPreviousChapter()

        // When
        verticalChapters.disable()

        // Then
        assertEquals(
            expected = VerticalChapterCounts(
                prepended = 0,
                appended = 0,
            ),
            actual = verticalChapters.requestedCounts.first(),
        )
    }

    @Test
    fun `GIVEN a disabled reader WHEN appending THEN waits for the next lookup instead of reusing the old one`() =
        runTest {
            // Given
            prepareScenario()
            verticalChapters.enable()
            verticalChapters.disable()

            // When
            verticalChapters.appendNextChapter()
            runCurrent()

            // Then
            assertTrue(verticalChapters.appended.value.isEmpty())
            assertEquals(
                expected = VerticalChapterCounts(
                    prepended = 0,
                    appended = 1,
                ),
                actual = verticalChapters.requestedCounts.first(),
            )
        }

    private fun genesis(chapterNumber: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = chapterNumber,
    )

    private fun suggestion(chapterNumber: Int): ReadNavigationSuggestionModel = ReadNavigationSuggestionModel(
        bookId = BookId.GEN,
        chapterNumber = chapterNumber,
    )

    private fun TestScope.prepareScenario(
        shouldForceCanonOrder: Boolean = false,
        getNextChapter: GetNextChapter = GetNextChapter { _, chapterNumber, _ -> suggestion(chapterNumber + 1) },
        getPreviousChapter: GetPreviousChapter = GetPreviousChapter { _, chapterNumber, _ ->
            suggestion(chapterNumber - 1)
        },
    ) {
        val recordedNextLookups = mutableListOf<ChapterLocationModel>()
        nextLookups = recordedNextLookups
        val recordedPreviousLookups = mutableListOf<ChapterLocationModel>()
        previousLookups = recordedPreviousLookups
        val recordedCanonOrderFlags = mutableListOf<Boolean>()
        canonOrderFlags = recordedCanonOrderFlags
        verticalChapters = ReadVerticalChaptersFactory(
            getNextChapter = { bookId, chapterNumber, isCanonOrderForced ->
                recordedNextLookups += ChapterLocationModel(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                )
                recordedCanonOrderFlags += isCanonOrderForced
                getNextChapter(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                    shouldForceCanonOrder = isCanonOrderForced,
                )
            },
            getPreviousChapter = { bookId, chapterNumber, isCanonOrderForced ->
                recordedPreviousLookups += ChapterLocationModel(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                )
                recordedCanonOrderFlags += isCanonOrderForced
                getPreviousChapter(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                    shouldForceCanonOrder = isCanonOrderForced,
                )
            },
        ).create(
            scope = backgroundScope,
            bookId = BookId.GEN,
            chapterNumber = 3,
            shouldForceCanonOrder = shouldForceCanonOrder,
        )
    }
}
