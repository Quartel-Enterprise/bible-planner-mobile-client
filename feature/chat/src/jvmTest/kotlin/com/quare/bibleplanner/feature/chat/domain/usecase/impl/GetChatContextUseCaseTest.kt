package com.quare.bibleplanner.feature.chat.domain.usecase.impl

import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayPassagesForDayStudyUseCase
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.chat.domain.model.ChatPlanDayModel
import com.quare.bibleplanner.feature.chat.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.chat.fake.FakePlanRepository
import kotlinx.coroutines.flow.flowOf
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
            weeksByPlan = mapOf(
                ReadingPlanType.CHRONOLOGICAL to weeks,
                ReadingPlanType.BOOKS to weeks,
            ),
            selectedPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        useCase = GetChatContextUseCase(
            getDayPassages = GetDayPassagesForDayStudyUseCase(
                GetPlansByWeekUseCase(
                    planRepository = planRepository,
                    booksRepository = FakeBooksRepository(flowOf(emptyList())),
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
    fun `GIVEN no day WHEN loading the chat context THEN there is none`() = runTest {
        // When
        val context = useCase(null)

        // Then
        assertNull(context)
    }

    @Test
    fun `GIVEN a day with a reading WHEN loading the chat context THEN labels its passages and keeps the day`() =
        runTest {
            // When
            val context = useCase(
                DayNavRoute(
                    dayNumber = 1,
                    weekNumber = 1,
                    readingPlanType = ReadingPlanType.CHRONOLOGICAL.name,
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
            DayNavRoute(
                dayNumber = 2,
                weekNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS.name,
            ),
        )

        // Then
        assertNull(context)
    }

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
