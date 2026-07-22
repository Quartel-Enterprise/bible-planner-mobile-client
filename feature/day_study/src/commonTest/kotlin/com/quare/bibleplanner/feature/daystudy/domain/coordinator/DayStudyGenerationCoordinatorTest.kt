package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DayStudyGenerationCoordinatorTest {
    @Test
    fun `GIVEN a stream WHEN start THEN a generating job appears then completes as done`() = runTest {
        // Given
        val repository = FakeDayStudyRepository(
            events = listOf(
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
                DayStudyGenerationEventModel.Completed(study),
            ),
        )
        val coordinator = coordinator(repository)

        // When
        coordinator.start(passages, dayRoute, LABEL)

        // Then — synchronously generating before the stream is driven
        val generating = coordinator.jobs.value.single()
        assertEquals(coordinator.keyOf(dayRoute), generating.key)
        assertEquals(DayStudyGenerationStatus.Generating, generating.status)

        advanceUntilIdle()
        val done = coordinator.jobs.value.single()
        assertEquals(DayStudyGenerationStatus.Done(study), done.status)
        assertEquals(DayStudyPhaseModel.CHAPTERS, done.phase)
    }

    @Test
    fun `GIVEN a running job WHEN starting the same day again THEN it is not duplicated`() = runTest {
        // Given
        val coordinator = coordinator(FakeDayStudyRepository(events = listOf()))

        // When
        coordinator.start(passages, dayRoute, LABEL)
        coordinator.start(passages, dayRoute, LABEL)

        // Then
        assertEquals(1, coordinator.jobs.value.size)
    }

    @Test
    fun `GIVEN two different days WHEN starting both THEN both jobs run concurrently`() = runTest {
        // Given
        val coordinator = coordinator(FakeDayStudyRepository(events = listOf()))

        // When
        coordinator.start(passages, dayRoute, LABEL)
        coordinator.start(passages, otherDayRoute, "Gênesis 2")

        // Then
        assertEquals(2, coordinator.jobs.value.size)
    }

    @Test
    fun `WHEN dismissing from card THEN the job keeps running but is marked dismissed`() = runTest {
        // Given
        val coordinator = coordinator(FakeDayStudyRepository(events = listOf()))
        val key = coordinator.start(passages, dayRoute, LABEL)

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
            FakeDayStudyRepository(events = emptyList(), error = IllegalStateException("boom")),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL)
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
    fun `WHEN acknowledging a job THEN it is removed`() = runTest {
        // Given
        val coordinator = coordinator(FakeDayStudyRepository(events = listOf()))
        val key = coordinator.start(passages, dayRoute, LABEL)

        // When
        coordinator.acknowledge(key)

        // Then
        assertTrue(coordinator.jobs.value.isEmpty())
    }

    @Test
    fun `GIVEN two generating jobs WHEN counting THEN excluded key is not counted`() = runTest {
        // Given
        val coordinator = coordinator(FakeDayStudyRepository(events = listOf()))
        coordinator.start(passages, dayRoute, LABEL)
        coordinator.start(passages, otherDayRoute, "Gênesis 2")

        // When / Then
        assertEquals(2, coordinator.generatingCount(excludingKey = null))
        assertEquals(1, coordinator.generatingCount(excludingKey = coordinator.keyOf(dayRoute)))
    }

    @Test
    fun `GIVEN a completed job WHEN counting THEN it is not counted as generating`() = runTest {
        // Given
        val coordinator = coordinator(
            FakeDayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
        )
        coordinator.start(passages, dayRoute, LABEL)

        // When
        advanceUntilIdle()

        // Then
        assertEquals(0, coordinator.generatingCount(excludingKey = null))
    }

    @Test
    fun `GIVEN a completing stream WHEN start THEN tracks day_study_generation_completed with the day params`() =
        runTest {
            // Given
            val coordinator = coordinator(
                FakeDayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL)
            advanceUntilIdle()

            // Then
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_completed" }
            assertEquals(dayRoute.readingPlanType, params["plan_type"])
            assertEquals(dayRoute.weekNumber, params["week_number"])
            assertEquals(dayRoute.dayNumber, params["day_number"])
            assertEquals(false, params["is_pro"])
        }

    @Test
    fun `GIVEN a completing stream WHEN start THEN tracks day_study_generation_time as success`() = runTest {
        // Given
        val coordinator = coordinator(
            FakeDayStudyRepository(events = listOf(DayStudyGenerationEventModel.Completed(study))),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL)
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
            FakeDayStudyRepository(
                events = listOf(
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
                    DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CONTEXT),
                    DayStudyGenerationEventModel.Completed(study),
                ),
            ),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL)
        advanceUntilIdle()

        // Then
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_time" }
        assertTrue(params["reading_ms"] is Long)
        assertTrue(params["chapters_ms"] is Long)
        assertTrue(params["context_ms"] is Long)
        assertTrue("questions_ms" !in params)
    }

    @Test
    fun `GIVEN a connection drop mid generation THEN the job fails as offline`() = runTest {
        // Given
        val coordinator = coordinator(
            FakeDayStudyRepository(
                events = listOf(DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING)),
                neverCompletes = true,
            ),
        )
        coordinator.start(passages, dayRoute, LABEL)

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
                FakeDayStudyRepository(events = emptyList(), error = LimitReachedException()),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL)
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
                FakeDayStudyRepository(events = emptyList(), error = LimitReachedException()),
            )

            // When
            coordinator.start(passages, dayRoute, LABEL)
            advanceUntilIdle()

            // Then
            val (_, params) = trackedEvents.single { it.first == "day_study_generation_failed" }
            assertEquals("limit_reached", params["reason"])
        }

    @Test
    fun `GIVEN a generic failure WHEN start THEN tracks day_study_generation_failed with error reason`() = runTest {
        // Given
        val coordinator = coordinator(
            FakeDayStudyRepository(events = emptyList(), error = IllegalStateException("boom")),
        )

        // When
        coordinator.start(passages, dayRoute, LABEL)
        advanceUntilIdle()

        // Then
        val (_, params) = trackedEvents.single { it.first == "day_study_generation_failed" }
        assertEquals("error", params["reason"])
    }

    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    private val isConnectedFlow = MutableStateFlow(true)

    private fun TestScope.coordinator(repository: FakeDayStudyRepository): DayStudyGenerationCoordinator =
        DayStudyGenerationCoordinator(
            applicationScope = ApplicationScope(this),
            getDayStudy = GetDayStudyUseCase(
                repository = repository,
                bibleRepository = FakeBibleRepository(),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
            observeIsProUser = { flowOf(false) },
            networkConnectivityObserver = { isConnectedFlow },
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

    private class FakeDayStudyRepository(
        private val events: List<DayStudyGenerationEventModel>,
        private val error: Throwable? = null,
        private val neverCompletes: Boolean = false,
    ) : DayStudyRepository {
        override fun getDayStudy(
            passages: List<PassageModel>,
            version: String,
            languageCode: String,
        ): Flow<DayStudyGenerationEventModel> = flow {
            events.forEach { emit(it) }
            error?.let { throw it }
            if (neverCompletes) awaitCancellation()
        }

        override suspend fun getDayStudyStatus(
            passages: List<PassageModel>,
            version: String,
            languageCode: String,
        ): DayStudyStatusModel? = null

        override suspend fun hasCachedStudy(
            passages: List<PassageModel>,
            version: String,
            languageCode: String,
        ): Boolean = false
    }

    private class FakeBibleRepository : BibleRepository {
        override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(emptyList())

        override fun getSelectedVersionIdFlow(): Flow<String> = flowOf("ACF")

        override suspend fun setSelectedVersionId(id: String) = Unit
    }
}
