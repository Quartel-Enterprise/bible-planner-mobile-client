package com.quare.bibleplanner.core.chapterlistening.domain.usecase

import com.quare.bibleplanner.core.chapterlistening.domain.mapper.createListeningSession
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class FindAdjacentListeningSegmentUseCaseTest {
    private val genesisOne = createChapter(1)
    private val genesisTwo = createChapter(2)
    private val genesisThree = createChapter(3)
    private val firstSegment = ListeningSegmentModel(
        chapter = genesisOne,
        startVerse = 1,
        endVerse = 10,
    )
    private val secondSegment = ListeningSegmentModel(
        chapter = genesisTwo,
        startVerse = null,
        endVerse = null,
    )
    private val day = ListeningDayModel(
        location = PlanDayLocationModel(
            weekNumber = 1,
            dayNumber = 1,
            readingPlanType = ReadingPlanType.BOOKS,
        ),
        segments = listOf(firstSegment, secondSegment),
    )

    private lateinit var useCase: FindAdjacentListeningSegmentUseCase
    private lateinit var adjacentRequests: MutableList<Triple<ChapterLocationModel, ChapterDirectionModel, Boolean>>

    @Test
    fun `GIVEN a day reading WHEN finding the next segment THEN returns the next segment of the day`() = runTest {
        // Given
        prepareScenario(adjacentChapter = genesisThree)

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.DAY_READING,
                day = day,
                segment = firstSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertEquals(secondSegment, segment)
        assertTrue(adjacentRequests.isEmpty())
    }

    @Test
    fun `GIVEN a day reading WHEN finding the previous segment THEN returns the previous segment of the day`() =
        runTest {
            // Given
            prepareScenario(adjacentChapter = genesisThree)

            // When
            val segment = useCase(
                current = createSession(
                    mode = ListeningModeModel.DAY_READING,
                    day = day,
                    segment = secondSegment,
                ),
                direction = ChapterDirectionModel.PREVIOUS,
            )

            // Then
            assertEquals(firstSegment, segment)
        }

    @Test
    fun `GIVEN the last segment of a day WHEN finding the next segment THEN there is none`() = runTest {
        // Given
        prepareScenario(adjacentChapter = genesisThree)

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.DAY_READING,
                day = day,
                segment = secondSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertNull(segment)
    }

    @Test
    fun `GIVEN a single chapter WHEN finding the next segment THEN returns the whole adjacent chapter`() = runTest {
        // Given
        prepareScenario(adjacentChapter = genesisTwo)

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.CHAPTER,
                day = null,
                segment = firstSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertEquals(secondSegment, segment)
        assertEquals(
            listOf(
                Triple(
                    first = genesisOne,
                    second = ChapterDirectionModel.NEXT,
                    third = true,
                ),
            ),
            adjacentRequests,
        )
    }

    @Test
    fun `GIVEN a day reading without its day WHEN finding the next segment THEN follows the reading order`() = runTest {
        // Given
        prepareScenario(adjacentChapter = genesisTwo)

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.DAY_READING,
                day = null,
                segment = firstSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertEquals(secondSegment, segment)
    }

    @Test
    fun `GIVEN the last chapter WHEN finding the next segment THEN there is none`() = runTest {
        // Given
        prepareScenario(adjacentChapter = null)

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.CHAPTER,
                day = null,
                segment = firstSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertNull(segment)
    }

    @Test
    fun `GIVEN the reading order cannot be read WHEN finding the next segment THEN there is none`() = runTest {
        // Given
        prepareScenario(
            adjacentChapter = genesisTwo,
            isFailing = true,
        )

        // When
        val segment = useCase(
            current = createSession(
                mode = ListeningModeModel.CHAPTER,
                day = null,
                segment = firstSegment,
            ),
            direction = ChapterDirectionModel.NEXT,
        )

        // Then
        assertNull(segment)
    }

    private fun createChapter(number: Int): ChapterLocationModel = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = number,
    )

    private fun createSession(
        mode: ListeningModeModel,
        day: ListeningDayModel?,
        segment: ListeningSegmentModel,
    ): ListeningSessionModel = createListeningSession(
        mode = mode,
        day = day,
        segment = segment,
        bibleVersionId = "ACF",
        shouldForceCanonOrder = true,
    )

    private fun prepareScenario(
        adjacentChapter: ChapterLocationModel?,
        isFailing: Boolean = false,
    ) {
        adjacentRequests = mutableListOf()
        useCase = FindAdjacentListeningSegmentUseCase(
            getAdjacentListeningChapter = { chapter, direction, shouldForceCanonOrder ->
                adjacentRequests += Triple(
                    first = chapter,
                    second = direction,
                    third = shouldForceCanonOrder,
                )
                if (isFailing) error("Reading order unavailable")
                adjacentChapter
            },
        )
    }
}
