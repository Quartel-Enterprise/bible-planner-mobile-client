package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.toWholeChapterSegment
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterAudioCacheKeeperTest {
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)

    private val testScope = TestScope()

    private lateinit var cacheKeeper: ChapterAudioCacheKeeper
    private lateinit var audioRepository: FakeChapterAudioRepository

    @BeforeTest
    fun setUp() {
        audioRepository = FakeChapterAudioRepository(emptyMap())
        cacheKeeper = ChapterAudioCacheKeeper(
            scope = testScope.backgroundScope,
            chapterAudioRepository = audioRepository,
        )
    }

    @Test
    fun `GIVEN an audio without verses already evicted WHEN evicting it again THEN keeps the cached file`() =
        testScope.runTest {
            // Given
            cacheKeeper.evictEmpty(GENESIS_ONE_PATH)
            runCurrent()

            // When
            cacheKeeper.evictEmpty(GENESIS_ONE_PATH)
            runCurrent()

            // Then
            assertEquals(listOf(GENESIS_ONE_PATH), audioRepository.evicted)
        }

    @Test
    fun `GIVEN an audio without verses evicted WHEN evicting another one THEN evicts it too`() = testScope.runTest {
        // Given
        cacheKeeper.evictEmpty(GENESIS_ONE_PATH)
        runCurrent()

        // When
        cacheKeeper.evictEmpty(GENESIS_TWO_PATH)
        runCurrent()

        // Then
        assertEquals(listOf(GENESIS_ONE_PATH, GENESIS_TWO_PATH), audioRepository.evicted)
    }

    @Test
    fun `GIVEN an unreadable audio already evicted WHEN evicting it again THEN evicts it every time`() =
        testScope.runTest {
            // Given
            cacheKeeper.evict(GENESIS_ONE_PATH)
            runCurrent()

            // When
            cacheKeeper.evict(GENESIS_ONE_PATH)
            runCurrent()

            // Then
            assertEquals(listOf(GENESIS_ONE_PATH, GENESIS_ONE_PATH), audioRepository.evicted)
        }

    @Test
    fun `GIVEN an eviction that has not run yet WHEN awaiting it THEN it has finished`() = testScope.runTest {
        // Given
        cacheKeeper.evict(GENESIS_ONE_PATH)

        // When
        cacheKeeper.awaitEviction()

        // Then
        assertEquals(listOf(GENESIS_ONE_PATH), audioRepository.evicted)
    }

    @Test
    fun `GIVEN no eviction WHEN awaiting it THEN returns without evicting`() = testScope.runTest {
        // When
        cacheKeeper.awaitEviction()

        // Then
        assertTrue(audioRepository.evicted.isEmpty())
    }

    @Test
    fun `GIVEN a chapter left WHEN refreshing it THEN checks its revision`() = testScope.runTest {
        // When
        cacheKeeper.refreshLeftChapter(createSession(genesisOne))
        runCurrent()

        // Then
        assertEquals(listOf(genesisOne), audioRepository.refreshed)
    }

    @Test
    fun `GIVEN a refresh not run yet WHEN another chapter is left THEN only checks the last one`() = testScope.runTest {
        // Given
        cacheKeeper.refreshLeftChapter(createSession(genesisOne))

        // When
        cacheKeeper.refreshLeftChapter(createSession(genesisTwo))
        runCurrent()

        // Then
        assertEquals(listOf(genesisTwo), audioRepository.refreshed)
    }

    @Test
    fun `GIVEN a refresh not run yet WHEN cancelling it THEN checks nothing`() = testScope.runTest {
        // Given
        cacheKeeper.refreshLeftChapter(createSession(genesisOne))

        // When
        cacheKeeper.cancelRefresh()
        runCurrent()

        // Then
        assertTrue(audioRepository.refreshed.isEmpty())
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

    private companion object {
        const val GENESIS_ONE_PATH = "/cache/GEN_1.mp3"
        const val GENESIS_TWO_PATH = "/cache/GEN_2.mp3"
    }
}
