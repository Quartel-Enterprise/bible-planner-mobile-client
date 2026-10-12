package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterAudioPrefetcherTest {
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)
    private val genesisThree = createChapter(3)
    private val readingOrder = listOf(genesisOne, genesisTwo, genesisThree)

    private lateinit var prefetcher: ChapterAudioPrefetcher
    private lateinit var audioRepository: FakeChapterAudioRepository
    private lateinit var accessGate: CompletableDeferred<Unit>

    @Test
    fun `GIVEN an open next chapter WHEN prefetching THEN downloads its audio`() = runTest {
        // Given
        prepareScenario()

        // When
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()

        // Then
        assertEquals(listOf(genesisTwo), audioRepository.requests)
    }

    @Test
    fun `GIVEN a locked next chapter WHEN prefetching THEN leaves it alone`() = runTest {
        // Given
        prepareScenario(lockedChapter = genesisTwo)

        // When
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()

        // Then
        assertTrue(audioRepository.requests.isEmpty())
    }

    @Test
    fun `GIVEN the last chapter WHEN prefetching THEN downloads nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        prefetcher.prefetchNext(createSession(genesisThree))
        runCurrent()

        // Then
        assertTrue(audioRepository.requests.isEmpty())
    }

    @Test
    fun `GIVEN the access check fails WHEN prefetching THEN downloads nothing`() = runTest {
        // Given
        prepareScenario(isAccessCheckFailing = true)

        // When
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()

        // Then
        assertTrue(audioRepository.requests.isEmpty())
    }

    @Test
    fun `GIVEN a prefetch of the next chapter WHEN that chapter starts loading THEN keeps the prefetch`() = runTest {
        // Given
        prepareScenario(isAccessGated = true)
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()
        accessGate.complete(Unit)

        // When
        prefetcher.cancelUnlessFor(genesisTwo)
        runCurrent()

        // Then
        assertEquals(listOf(genesisTwo), audioRepository.requests)
    }

    @Test
    fun `GIVEN a prefetch of the next chapter WHEN another chapter starts loading THEN cancels the prefetch`() =
        runTest {
            // Given
            prepareScenario(isAccessGated = true)
            prefetcher.prefetchNext(createSession(genesisOne))
            runCurrent()
            accessGate.complete(Unit)

            // When
            prefetcher.cancelUnlessFor(genesisThree)
            runCurrent()

            // Then
            assertTrue(audioRepository.requests.isEmpty())
        }

    @Test
    fun `GIVEN a prefetch in progress WHEN cancelling it THEN downloads nothing`() = runTest {
        // Given
        prepareScenario(isAccessGated = true)
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()
        accessGate.complete(Unit)

        // When
        prefetcher.cancel()
        runCurrent()

        // Then
        assertTrue(audioRepository.requests.isEmpty())
    }

    @Test
    fun `GIVEN a prefetch in progress WHEN prefetching after another chapter THEN only downloads the newest next`() =
        runTest {
            // Given
            prepareScenario(isAccessGated = true)
            prefetcher.prefetchNext(createSession(genesisOne))
            runCurrent()
            accessGate.complete(Unit)

            // When
            prefetcher.prefetchNext(createSession(genesisTwo))
            runCurrent()

            // Then
            assertEquals(listOf(genesisThree), audioRepository.requests)
        }

    @Test
    fun `GIVEN a replaced prefetch WHEN the new next chapter starts loading THEN keeps the new prefetch`() = runTest {
        // Given
        prepareScenario(isAccessGated = true)
        prefetcher.prefetchNext(createSession(genesisOne))
        runCurrent()
        prefetcher.prefetchNext(createSession(genesisTwo))
        runCurrent()
        accessGate.complete(Unit)

        // When
        prefetcher.cancelUnlessFor(genesisThree)
        runCurrent()

        // Then
        assertEquals(listOf(genesisThree), audioRepository.requests)
    }

    private fun createSession(chapter: ChapterLocationModel): ListeningSessionModel = createListeningSession(
        mode = ListeningModeModel.CHAPTER,
        day = null,
        segment = chapter.toWholeChapterSegment(),
        bibleVersionId = "ACF",
        shouldForceCanonOrder = false,
    )

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )

    private fun TestScope.prepareScenario(
        lockedChapter: ChapterLocationModel? = null,
        isAccessGated: Boolean = false,
        isAccessCheckFailing: Boolean = false,
    ) {
        accessGate = CompletableDeferred()
        if (!isAccessGated) accessGate.complete(Unit)
        audioRepository = FakeChapterAudioRepository(
            readingOrder.associateWith { chapter ->
                ChapterAudioModel(
                    audioPath = "/cache/GEN_${chapter.chapterNumber}.mp3",
                    titleWords = emptyList(),
                    verses = emptyList(),
                    duration = Duration.ZERO,
                )
            },
        )
        prefetcher = ChapterAudioPrefetcher(
            scope = backgroundScope,
            chapterAudioRepository = audioRepository,
            findAdjacentSegment = FindAdjacentListeningSegmentUseCase { chapter, direction, _ ->
                val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
                readingOrder.getOrNull(readingOrder.indexOf(chapter) + step)
            },
            getChapterListeningAccess = { chapter ->
                accessGate.await()
                if (isAccessCheckFailing) error("Remote config unavailable")
                if (chapter == lockedChapter) {
                    ChapterListeningAccessModel.LoginRequired
                } else {
                    ChapterListeningAccessModel.Open
                }
            },
        )
    }
}
