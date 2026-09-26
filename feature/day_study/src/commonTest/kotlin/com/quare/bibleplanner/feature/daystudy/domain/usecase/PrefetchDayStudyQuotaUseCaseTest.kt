package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.store.DayStudyQuotaPrefetchStore
import com.quare.bibleplanner.feature.daystudy.fake.DefaultIntRemoteConfig
import com.quare.bibleplanner.feature.daystudy.fake.FakeBibleRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakeDayStudyRepository
import com.quare.bibleplanner.feature.daystudy.fake.FakePlanRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PrefetchDayStudyQuotaUseCaseTest {
    private val day = PlanDayLocationModel(
        weekNumber = 1,
        dayNumber = 2,
        readingPlanType = ReadingPlanType.CHRONOLOGICAL,
    )
    private lateinit var useCase: PrefetchDayStudyQuotaUseCase
    private lateinit var store: DayStudyQuotaPrefetchStore

    @Test
    fun `GIVEN a planned day WHEN prefetching THEN stores its quota for later`() = runTest {
        // Given
        prepareScenario(statusError = null)

        // When
        useCase(day)

        // Then
        assertEquals(
            DayStudyQuotaModel(
                freeLimit = 3,
                remainingFree = 1,
                isUnlockedForDay = false,
                hasLocalStudy = false,
            ),
            store.findQuota(day),
        )
    }

    @Test
    fun `GIVEN a day outside the plan WHEN prefetching THEN stores nothing`() = runTest {
        // Given
        prepareScenario(statusError = null)
        val missingDay = day.copy(dayNumber = 7)

        // When
        useCase(missingDay)

        // Then
        assertNull(store.findQuota(missingDay))
    }

    @Test
    fun `GIVEN the quota request fails WHEN prefetching THEN swallows the failure and stores nothing`() = runTest {
        // Given
        prepareScenario(statusError = IllegalStateException("offline"))

        // When
        useCase(day)

        // Then
        assertNull(store.findQuota(day))
    }

    private fun prepareScenario(statusError: Throwable?) {
        store = DayStudyQuotaPrefetchStore()
        val repository = FakeDayStudyRepository(
            hasCached = false,
            status = DayStudyStatusModel(
                freeLimit = 3,
                usedCount = 2,
                isUnlocked = false,
                cacheToken = "token",
            ),
            statusError = statusError,
            events = emptyList(),
        )
        useCase = PrefetchDayStudyQuotaUseCase(
            getDayPassagesForDayStudy = GetDayPassagesForDayStudyUseCase(
                GetPlansByWeekUseCase(
                    planRepository = FakePlanRepository(
                        listOf(
                            WeekPlanModel(
                                number = 1,
                                days = listOf(
                                    DayModel(
                                        number = 2,
                                        passages = listOf(
                                            PassageModel(
                                                bookId = BookId.JON,
                                                chapters = emptyList(),
                                                isRead = false,
                                                chapterRanges = null,
                                            ),
                                        ),
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
            ),
            getDayStudyQuota = GetDayStudyQuotaUseCase(
                repository = repository,
                bibleRepository = FakeBibleRepository(),
                getAppLanguageFlow = { flowOf(Language.ENGLISH) },
                languageCodeMapper = LanguageCodeMapper(),
                getIntRemoteConfig = DefaultIntRemoteConfig(),
            ),
            quotaPrefetchStore = store,
        )
    }
}
