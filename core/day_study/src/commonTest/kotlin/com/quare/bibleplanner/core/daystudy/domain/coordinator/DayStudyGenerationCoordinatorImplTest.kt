package com.quare.bibleplanner.core.daystudy.domain.coordinator

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.core.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyUseCase
import com.quare.bibleplanner.core.daystudy.testing.FakeDayStudyRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DayStudyGenerationCoordinatorImplTest {
    @Test
    fun `GIVEN a stream WHEN start THEN a generating job appears before the stream is driven`() = runTest {
        // Given
        val repository = dayStudyRepository(
            events = listOf(
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
                DayStudyGenerationEventModel.Completed(study),
            ),
        )
        val coordinator = coordinator(repository)

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // Then
        val generating = coordinator.jobs.value.single()
        assertEquals(coordinator.keyOf(dayRoute), generating.key)
        assertEquals(DayStudyGenerationStatus.Generating, generating.status)
    }

    @Test
    fun `GIVEN a started stream WHEN the stream is driven THEN the job completes as done`() = runTest {
        // Given
        val repository = dayStudyRepository(
            events = listOf(
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
                DayStudyGenerationEventModel.Completed(study),
            ),
        )
        val coordinator = coordinator(repository)
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // When
        advanceUntilIdle()

        // Then
        val done = coordinator.jobs.value.single()
        assertEquals(DayStudyGenerationStatus.Done(study), done.status)
        assertEquals(DayStudyPhaseModel.CHAPTERS, done.phase)
    }

    @Test
    fun `GIVEN a running job WHEN starting the same day again THEN it is not duplicated`() = runTest {
        // Given
        val coordinator = coordinator(dayStudyRepository(events = listOf()))

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // Then
        assertEquals(1, coordinator.jobs.value.size)
    }

    @Test
    fun `GIVEN two different days WHEN starting both THEN both jobs run concurrently`() = runTest {
        // Given
        val coordinator = coordinator(dayStudyRepository(events = listOf()))

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        coordinator.start(passages, otherDayRoute, "Gênesis 2", isRewarded = false)

        // Then
        assertEquals(2, coordinator.jobs.value.size)
    }

    @Test
    fun `GIVEN a running job WHEN dismissing it from the card THEN it keeps running but is marked dismissed`() =
        runTest {
            // Given
            val coordinator = coordinator(dayStudyRepository(events = listOf()))
            val key = coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

            // When
            coordinator.dismissFromCard(key)

            // Then
            assertTrue(key in coordinator.dismissedKeys.value)
            assertEquals(1, coordinator.jobs.value.size)
        }

    @Test
    fun `GIVEN a failing stream WHEN start THEN the job ends as failed`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(events = emptyList(), eventsError = IllegalStateException("boom")),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        advanceUntilIdle()

        // Then
        assertEquals(
            DayStudyGenerationStatus.Failed(
                isLimitReached = false,
                isOffline = false,
            ),
            coordinator.jobs.value
                .single()
                .status,
        )
    }

    @Test
    fun `GIVEN a rewarded unlock WHEN it completes THEN asks for a rewarded study and leaves no reward unserved`() =
        runTest {
            // Given
            val repository = dayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study)))
            val coordinator = coordinator(repository)

            // When
            val key = coordinator.start(passages, dayRoute, LABEL, isRewarded = true)
            advanceUntilIdle()

            // Then
            assertEquals(listOf(true), repository.studyRewardFlags)
            assertFalse(coordinator.hasUnservedReward(key))
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_completed" }
            assertEquals(true, params["is_rewarded"])
        }

    @Test
    fun `GIVEN a rewarded unlock WHEN the generation fails THEN the reward stays unserved for a free retry`() =
        runTest {
            // Given
            val coordinator = coordinator(
                dayStudyRepository(events = emptyList(), eventsError = IllegalStateException("boom")),
            )

            // When
            val key = coordinator.start(passages, dayRoute, LABEL, isRewarded = true)
            advanceUntilIdle()

            // Then
            assertTrue(coordinator.hasUnservedReward(key))
        }

    @Test
    fun `GIVEN a rewarded unlock WHEN the server refuses it THEN the reward is dropped`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(events = emptyList(), eventsError = LimitReachedException()),
        )

        // When
        val key = coordinator.start(passages, dayRoute, LABEL, isRewarded = true)
        advanceUntilIdle()

        // Then
        assertFalse(coordinator.hasUnservedReward(key))
    }

    @Test
    fun `GIVEN a running job WHEN acknowledging it THEN it is removed`() = runTest {
        // Given
        val coordinator = coordinator(dayStudyRepository(events = listOf()))
        val key = coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // When
        coordinator.acknowledge(key)

        // Then
        assertTrue(coordinator.jobs.value.isEmpty())
    }

    @Test
    fun `GIVEN two generating jobs WHEN counting THEN excluded key is not counted`() = runTest {
        // Given
        val coordinator = coordinator(dayStudyRepository(events = listOf()))
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        coordinator.start(passages, otherDayRoute, "Gênesis 2", isRewarded = false)

        // When
        val generatingCount = coordinator.getGeneratingCount(excludingKey = null)
        val generatingCountExcludingDay = coordinator.getGeneratingCount(excludingKey = coordinator.keyOf(dayRoute))

        // Then
        assertEquals(2, generatingCount)
        assertEquals(1, generatingCountExcludingDay)
    }

    @Test
    fun `GIVEN a completed job WHEN counting THEN it is not counted as generating`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
        )
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // When
        advanceUntilIdle()

        // Then
        assertEquals(0, coordinator.getGeneratingCount(excludingKey = null))
    }

    @Test
    fun `GIVEN a completing stream WHEN start THEN tracks day_study_generation_completed with the day params`() =
        runTest {
            // Given
            val coordinator = coordinator(
                dayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
            advanceUntilIdle()

            // Then
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_completed" }
            assertEquals("one_year", params["plan_type"])
            assertEquals(dayRoute.weekNumber, params["week_number"])
            assertEquals(dayRoute.dayNumber, params["day_number"])
            assertEquals(false, params["is_pro"])
        }

    @Test
    fun `GIVEN a completing stream WHEN start THEN tracks day_study_generation_time as success`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        advanceUntilIdle()

        // Then
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_time" }
        assertEquals(true, params["success"])
        assertEquals(false, params["is_pro"])
        assertTrue(params["duration_ms"] is Long)
        assertTrue("reason" !in params)
    }

    @Test
    fun `GIVEN reported phases WHEN generation completes THEN generation_time carries per-phase durations`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(
                events = listOf(
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CONTEXT),
                    DayStudyGenerationEventModel.Completed(study),
                ),
            ),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        advanceUntilIdle()

        // Then
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_time" }
        assertTrue(params["reading_ms"] is Long)
        assertTrue(params["chapters_ms"] is Long)
        assertTrue(params["context_ms"] is Long)
        assertTrue("questions_ms" !in params)
    }

    @Test
    fun `GIVEN a generation in progress WHEN the connection drops THEN the job fails as offline`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(
                events = listOf(DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING)),
                neverCompletes = true,
            ),
        )
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)

        // When
        isConnectedFlow.value = false
        advanceUntilIdle()

        // Then
        assertEquals(
            DayStudyGenerationStatus.Failed(
                isLimitReached = false,
                isOffline = true,
            ),
            coordinator.jobs.value
                .single()
                .status,
        )
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_time" }
        assertEquals(false, params["success"])
        assertEquals("offline", params["reason"])
    }

    @Test
    fun `GIVEN a limit reached failure WHEN start THEN tracks day_study_generation_time with limit_reached`() =
        runTest {
            // Given
            val coordinator = coordinator(
                dayStudyRepository(events = emptyList(), eventsError = LimitReachedException()),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
            advanceUntilIdle()

            // Then
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_time" }
            assertEquals(false, params["success"])
            assertEquals("limit_reached", params["reason"])
        }

    @Test
    fun `GIVEN a limit reached failure WHEN start THEN tracks day_study_generation_failed with limit_reached`() =
        runTest {
            // Given
            val coordinator = coordinator(
                dayStudyRepository(events = emptyList(), eventsError = LimitReachedException()),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
            advanceUntilIdle()

            // Then
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_failed" }
            assertEquals("limit_reached", params["reason"])
        }

    @Test
    fun `GIVEN a generic failure WHEN start THEN tracks day_study_generation_failed with error reason`() = runTest {
        // Given
        val coordinator = coordinator(
            dayStudyRepository(events = emptyList(), eventsError = IllegalStateException("boom")),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL, isRewarded = false)
        advanceUntilIdle()

        // Then
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_failed" }
        assertEquals("error", params["reason"])
    }

    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    private val isConnectedFlow = MutableStateFlow(true)

    private fun dayStudyRepository(
        events: List<DayStudyGenerationEventModel>,
        eventsError: Throwable? = null,
        neverCompletes: Boolean = false,
    ): FakeDayStudyRepository = FakeDayStudyRepository(
        hasCached = false,
        status = null,
        statusError = null,
        events = events,
    ).apply {
        this.eventsError = eventsError
        this.neverCompletes = neverCompletes
    }

    private fun TestScope.coordinator(repository: FakeDayStudyRepository): DayStudyGenerationCoordinator =
        DayStudyGenerationCoordinatorImpl(
            applicationScope = ApplicationScope(this),
            getDayStudy = GetDayStudyUseCase(
                repository = repository,
                bibleRepository = FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "ACF",
                ),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
            observeIsProUser = { flowOf(false) },
            networkConnectivityObserver = { isConnectedFlow },
            isConnected = { isConnectedFlow.value },
            trackEvent = { name, params -> trackedEvents += name to params },
        )

    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(ChapterModel(number = 1, startVerse = null, endVerse = null, bookId = BookId.GEN)),
            isRead = false,
            chapterRanges = null,
        ),
    )
    private val dayRoute = DayNavRoute(dayNumber = 1, weekNumber = 1, readingPlanType = "ONE_YEAR")
    private val otherDayRoute = DayNavRoute(dayNumber = 2, weekNumber = 1, readingPlanType = "ONE_YEAR")
    private val study = DayStudyModel(
        passageLabel = "Gênesis 1",
        overview = "overview",
        chapterSummaries = emptyList(),
        takeaways = emptyList(),
        context = HistoricalContextModel(body = "body", facts = emptyList()),
        commonQuestions = emptyList(),
    )

    private companion object {
        const val LABEL = "Gênesis 1"
    }
}
