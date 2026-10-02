package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.plan.testing.FakePlanRepository
import com.quare.bibleplanner.feature.chat.domain.model.ChatContextModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.getString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetChatContextUseCaseTest {
    private val genesisPassage = PassageModel(
        bookId = BookId.GEN,
        chapters = listOf(4, 5).map { number ->
            ChapterModel(
                number = number,
                startVerse = null,
                endVerse = null,
                bookId = BookId.GEN,
            )
        },
        isRead = false,
        chapterRanges = "4-5",
    )
    private lateinit var useCase: GetChatContextUseCase

    @BeforeTest
    fun setUp() {
        val weeks = listOf(
            WeekPlanModel(
                number = 1,
                days = listOf(
                    day(
                        number = 1,
                        passages = listOf(genesisPassage),
                    ),
                    day(
                        number = 2,
                        passages = emptyList(),
                    ),
                ),
            ),
        )
        val planRepository = FakePlanRepository(
            plans = mapOf(
                ReadingPlanType.CHRONOLOGICAL to weeks,
                ReadingPlanType.BOOKS to weeks,
            ),
            startDate = null,
            selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        useCase = GetChatContextUseCase(
            getDayPassages = GetDayPassagesForDayStudyUseCase(
                GetPlansByWeekUseCase(
                    planRepository = planRepository,
                    booksRepository = FakeBooksRepository(emptyList()),
                    getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                    currentTimestampProvider = { 0L },
                    localDateTimeProvider = {
                        LocalDateTime(
                            year = 2026,
                            month = 1,
                            day = 1,
                            hour = 0,
                            minute = 0,
                        )
                    },
                ),
            ),
        )
    }

    @Test
    fun `GIVEN neither a day nor a chapter WHEN loading the chat context THEN there is none`() = runTest {
        // When
        val context = useCase(
            chatRoute(
                dayNumber = null,
                bookId = null,
                chapterNumber = null,
            ),
        )

        // Then
        assertNull(context)
    }

    @Test
    fun `GIVEN a day with a reading WHEN loading the chat context THEN labels its passages and keeps the day`() =
        runTest {
            // When
            val context = useCase(
                chatRoute(
                    dayNumber = 1,
                    bookId = null,
                    chapterNumber = null,
                ),
            )

            // Then
            assertEquals(
                expected = "${getString(BookId.GEN.toBookNameResource())} 4-5",
                actual = context?.label,
            )
            assertEquals(
                expected = listOf(BookId.GEN),
                actual = context?.passages?.map(PassageModel::bookId),
            )
            assertEquals(
                expected = ChatPlanDayModel(
                    dayNumber = 1,
                    weekNumber = 1,
                    readingPlanType = "CHRONOLOGICAL",
                ),
                actual = context?.planDay,
            )
        }

    @Test
    fun `GIVEN a day without passages WHEN loading the chat context THEN there is none`() = runTest {
        // When
        val context = useCase(
            chatRoute(
                dayNumber = 2,
                bookId = null,
                chapterNumber = null,
            ),
        )

        // Then
        assertNull(context)
    }

    @Test
    fun `GIVEN a chapter WHEN loading the chat context THEN it is that single chapter without a plan day`() = runTest {
        // When
        val context = useCase(
            chatRoute(
                dayNumber = null,
                bookId = BookId.GEN.name,
                chapterNumber = 3,
            ),
        )

        // Then
        assertEquals(
            expected = ChatContextModel(
                label = "${getString(BookId.GEN.toBookNameResource())} 3",
                passages = listOf(
                    PassageModel(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            ChapterModel(
                                number = 3,
                                startVerse = null,
                                endVerse = null,
                                bookId = BookId.GEN,
                            ),
                        ),
                        isRead = false,
                        chapterRanges = "3",
                    ),
                ),
                planDay = null,
            ),
            actual = context,
        )
    }

    @Test
    fun `GIVEN a day and a chapter WHEN loading the chat context THEN the day wins`() = runTest {
        // When
        val context = useCase(
            chatRoute(
                dayNumber = 1,
                bookId = BookId.EXO.name,
                chapterNumber = 3,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(BookId.GEN),
            actual = context?.passages?.map(PassageModel::bookId),
        )
    }

    @Test
    fun `GIVEN only a book or only a chapter WHEN loading the chat context THEN there is none`() = runTest {
        // When
        val contexts = listOf(
            useCase(
                chatRoute(
                    dayNumber = null,
                    bookId = BookId.GEN.name,
                    chapterNumber = null,
                ),
            ),
            useCase(
                chatRoute(
                    dayNumber = null,
                    bookId = null,
                    chapterNumber = 3,
                ),
            ),
        )

        // Then
        assertEquals(
            expected = listOf(null, null),
            actual = contexts,
        )
    }

    private fun chatRoute(
        dayNumber: Int?,
        bookId: String?,
        chapterNumber: Int?,
    ): ChatNavRoute = ChatNavRoute(
        source = ChatEntrySource.DAY_FAB,
        dayNumber = dayNumber,
        weekNumber = dayNumber?.let { 1 },
        readingPlanType = dayNumber?.let { ReadingPlanType.CHRONOLOGICAL.name },
        bookId = bookId,
        chapterNumber = chapterNumber,
    )

    private fun day(
        number: Int,
        passages: List<PassageModel>,
    ): DayModel = DayModel(
        number = number,
        passages = passages,
        isRead = false,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = null,
        plannedReadDate = null,
        notes = null,
        isToday = false,
    )
}
