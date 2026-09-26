package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.daystudy.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakePlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetDayPassagesForDayStudyUseCaseTest {
    private val passage = PassageModel(
        bookId = BookId.RUT,
        chapters = emptyList(),
        isRead = false,
        chapterRanges = null,
    )
    private lateinit var useCase: GetDayPassagesForDayStudyUseCase

    @BeforeTest
    fun setUp() {
        useCase = GetDayPassagesForDayStudyUseCase(
            GetPlansByWeekUseCase(
                planRepository = FakePlanRepository(
                    listOf(
                        WeekPlanModel(
                            number = 4,
                            days = listOf(
                                DayModel(
                                    number = 6,
                                    passages = listOf(passage),
                                    isRead = false,
                                    totalVerses = 0,
                                    readVerses = 0,
                                    readTimestamp = null,
                                    plannedReadDate = null,
                                    notes = null,
                                    isToday = false,
                                ),
                            ),
                        ),
                    ),
                ),
                booksRepository = FakeBooksRepository(),
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(LocalDate(2026, 1, 1), LocalTime(8, 0)) },
            ),
        )
    }

    @Test
    fun `GIVEN a planned day WHEN observing its passages THEN emits them`() = runTest {
        // When
        val passages = useCase(
            weekNumber = 4,
            dayNumber = 6,
            readingPlanType = ReadingPlanType.BOOKS,
        ).first()

        // Then
        assertEquals(listOf(BookId.RUT), passages?.map(PassageModel::bookId))
    }

    @Test
    fun `GIVEN a missing week or day WHEN observing its passages THEN emits null`() = runTest {
        // When
        val missing = listOf(
            useCase(
                weekNumber = 9,
                dayNumber = 6,
                readingPlanType = ReadingPlanType.BOOKS,
            ).first(),
            useCase(
                weekNumber = 4,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.BOOKS,
            ).first(),
        )

        // Then
        assertEquals(listOf<List<PassageModel>?>(null, null), missing)
    }
}
