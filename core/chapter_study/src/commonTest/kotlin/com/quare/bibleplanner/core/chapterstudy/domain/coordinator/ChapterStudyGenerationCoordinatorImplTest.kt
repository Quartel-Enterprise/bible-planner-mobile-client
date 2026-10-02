package com.quare.bibleplanner.core.chapterstudy.domain.coordinator

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationJob
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationStatus
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.ChapterStudyScopeResolver
import com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl.GenerateChapterStudyUseCase
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChapterStudyGenerationCoordinatorImplTest {
    private val connectivityPollInterval = 3.seconds
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val otherTarget = ChapterStudyTargetModel(
        bookId = BookId.ROM,
        chapterNumber = 5,
    )
    private val study = createChapterStudy()
    private val observedConnectivity = MutableStateFlow(true)
    private val polledConnectivity = MutableStateFlow(true)
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
    private lateinit var coordinator: ChapterStudyGenerationCoordinatorImpl
    private lateinit var repository: FakeChapterStudyRepository

    @Test
    fun `WHEN starting THEN a generating job without a phase appears right away`() = runTest {
        // Given
        prepareScenario(neverCompletes = true)

        // When
        coordinator.start(target)

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationJob(
                    target = target,
                    phase = null,
                    status = ChapterStudyGenerationStatus.Generating,
                ),
            ),
            actual = coordinator.jobs.value,
        )
    }

    @Test
    fun `GIVEN reported phases WHEN generating THEN the job shows the latest phase`() = runTest {
        // Given
        prepareScenario(
            events = listOf(
                ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.READING),
                ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.SUMMARY),
            ),
            neverCompletes = true,
        )

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationJob(
                    target = target,
                    phase = ChapterStudyPhaseModel.SUMMARY,
                    status = ChapterStudyGenerationStatus.Generating,
                ),
            ),
            actual = coordinator.jobs.value,
        )
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a completing stream WHEN generating THEN the job ends as done with the study`() = runTest {
        // Given
        prepareScenario(
            events = listOf(
                ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.QUESTIONS),
                ChapterStudyGenerationEventModel.Completed(study),
            ),
        )

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationJob(
                    target = target,
                    phase = ChapterStudyPhaseModel.QUESTIONS,
                    status = ChapterStudyGenerationStatus.Done(study),
                ),
            ),
            actual = coordinator.jobs.value,
        )
    }

    @Test
    fun `GIVEN a completing stream WHEN generating THEN tracks the completion with the chapter params`() = runTest {
        // Given
        prepareScenario(
            events = listOf(ChapterStudyGenerationEventModel.Completed(study)),
            isPro = true,
        )

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        val (name, params) = trackedEvents.single()
        assertEquals(
            expected = "chapter_study_generation_completed",
            actual = name,
        )
        assertEquals(
            expected = setOf("book_id", "chapter_number", "is_pro", "duration_ms"),
            actual = params.keys,
        )
        assertEquals(
            expected = "GEN",
            actual = params["book_id"],
        )
        assertEquals(
            expected = 3,
            actual = params["chapter_number"],
        )
        assertEquals(
            expected = true,
            actual = params["is_pro"],
        )
        assertIs<Long>(params["duration_ms"])
    }

    @Test
    fun `GIVEN a failing stream WHEN generating THEN the job fails and tracks the error reason`() = runTest {
        // Given
        prepareScenario(eventsError = IllegalStateException("boom"))

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = ChapterStudyGenerationStatus.Failed(
                isLimitReached = false,
                isOffline = false,
            ),
            actual = coordinator.jobs.value
                .single()
                .status,
        )
        val (name, params) = trackedEvents.single()
        assertEquals(
            expected = "chapter_study_generation_failed",
            actual = name,
        )
        assertEquals(
            expected = "error",
            actual = params["reason"],
        )
        assertEquals(
            expected = "GEN",
            actual = params["book_id"],
        )
        assertEquals(
            expected = 3,
            actual = params["chapter_number"],
        )
        assertEquals(
            expected = false,
            actual = params["is_pro"],
        )
        assertIs<Long>(params["duration_ms"])
    }

    @Test
    fun `GIVEN the free limit is reached WHEN generating THEN the job fails as limit reached`() = runTest {
        // Given
        prepareScenario(eventsError = LimitReachedException())

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = ChapterStudyGenerationStatus.Failed(
                isLimitReached = true,
                isOffline = false,
            ),
            actual = coordinator.jobs.value
                .single()
                .status,
        )
        val (name, params) = trackedEvents.single()
        assertEquals(
            expected = "chapter_study_generation_failed",
            actual = name,
        )
        assertEquals(
            expected = "limit_reached",
            actual = params["reason"],
        )
    }

    @Test
    fun `GIVEN a generation in flight WHEN the device goes offline THEN the job fails as offline`() = runTest {
        // Given
        prepareScenario(
            events = listOf(ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.READING)),
            neverCompletes = true,
        )
        coordinator.start(target)
        runCurrent()

        // When
        observedConnectivity.value = false
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationJob(
                    target = target,
                    phase = ChapterStudyPhaseModel.READING,
                    status = ChapterStudyGenerationStatus.Failed(
                        isLimitReached = false,
                        isOffline = true,
                    ),
                ),
            ),
            actual = coordinator.jobs.value,
        )
        val (name, params) = trackedEvents.single()
        assertEquals(
            expected = "chapter_study_generation_failed",
            actual = name,
        )
        assertEquals(
            expected = "offline",
            actual = params["reason"],
        )
    }

    @Test
    fun `GIVEN the observer misses the drop WHEN the connectivity poll sees it THEN the job fails as offline`() =
        runTest {
            // Given
            prepareScenario(neverCompletes = true)
            coordinator.start(target)
            runCurrent()

            // When
            polledConnectivity.value = false
            advanceTimeBy(connectivityPollInterval)
            runCurrent()

            // Then
            assertEquals(
                expected = ChapterStudyGenerationStatus.Failed(
                    isLimitReached = false,
                    isOffline = true,
                ),
                actual = coordinator.jobs.value
                    .single()
                    .status,
            )
        }

    @Test
    fun `GIVEN a finished generation WHEN the device goes offline THEN the job stays done`() = runTest {
        // Given
        prepareScenario(events = listOf(ChapterStudyGenerationEventModel.Completed(study)))
        coordinator.start(target)
        runCurrent()

        // When
        observedConnectivity.value = false
        runCurrent()

        // Then
        assertEquals(
            expected = ChapterStudyGenerationStatus.Done(study),
            actual = coordinator.jobs.value
                .single()
                .status,
        )
        assertEquals(
            expected = listOf("chapter_study_generation_completed"),
            actual = trackedEvents.map { (name, _) -> name },
        )
    }

    @Test
    fun `GIVEN a generation in flight WHEN starting the same chapter again THEN only one generation runs`() = runTest {
        // Given
        prepareScenario(neverCompletes = true)
        coordinator.start(target)
        runCurrent()

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = 1,
            actual = coordinator.jobs.value.size,
        )
        assertEquals(
            expected = 1,
            actual = repository.generationRequests.size,
        )
    }

    @Test
    fun `GIVEN a failed generation WHEN starting the same chapter again THEN it generates again`() = runTest {
        // Given
        prepareScenario(eventsError = IllegalStateException("boom"))
        coordinator.start(target)
        runCurrent()
        repository.eventsError = null
        repository.events = listOf(ChapterStudyGenerationEventModel.Completed(study))

        // When
        coordinator.start(target)
        runCurrent()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyGenerationJob(
                    target = target,
                    phase = null,
                    status = ChapterStudyGenerationStatus.Done(study),
                ),
            ),
            actual = coordinator.jobs.value,
        )
        assertEquals(
            expected = 2,
            actual = repository.generationRequests.size,
        )
    }

    @Test
    fun `GIVEN two jobs WHEN acknowledging one THEN only that job is removed`() = runTest {
        // Given
        prepareScenario(neverCompletes = true)
        coordinator.start(target)
        coordinator.start(otherTarget)

        // When
        coordinator.acknowledge(target)

        // Then
        assertEquals(
            expected = listOf(otherTarget),
            actual = coordinator.jobs.value.map(ChapterStudyGenerationJob::target),
        )
    }

    @Test
    fun `GIVEN two generating jobs WHEN counting THEN the excluded chapter is not counted`() = runTest {
        // Given
        prepareScenario(neverCompletes = true)
        coordinator.start(target)
        coordinator.start(otherTarget)

        // When
        val countWithoutTarget = coordinator.getGeneratingCount(excluding = target)
        val countWithoutUnrelated = coordinator.getGeneratingCount(
            excluding = ChapterStudyTargetModel(
                bookId = BookId.PSA,
                chapterNumber = 23,
            ),
        )

        // Then
        assertEquals(
            expected = 1,
            actual = countWithoutTarget,
        )
        assertEquals(
            expected = 2,
            actual = countWithoutUnrelated,
        )
    }

    @Test
    fun `GIVEN a finished job WHEN counting THEN it is not counted as generating`() = runTest {
        // Given
        prepareScenario(events = listOf(ChapterStudyGenerationEventModel.Completed(study)))
        coordinator.start(target)
        runCurrent()

        // When
        val count = coordinator.getGeneratingCount(excluding = otherTarget)

        // Then
        assertEquals(
            expected = 0,
            actual = count,
        )
    }

    private fun TestScope.prepareScenario(
        events: List<ChapterStudyGenerationEventModel> = emptyList(),
        eventsError: Throwable? = null,
        neverCompletes: Boolean = false,
        isPro: Boolean = false,
    ) {
        repository = FakeChapterStudyRepository(
            cachedStudy = null,
            status = null,
            events = events,
        ).apply {
            this.eventsError = eventsError
            this.neverCompletes = neverCompletes
        }
        coordinator = ChapterStudyGenerationCoordinatorImpl(
            applicationScope = ApplicationScope(backgroundScope),
            generateChapterStudy = GenerateChapterStudyUseCase(
                repository = repository,
                scopeResolver = ChapterStudyScopeResolver(
                    bibleRepository = FakeBibleRepository(
                        bibles = emptyList(),
                        selectedVersionId = "ACF",
                    ),
                    getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                    languageCodeMapper = LanguageCodeMapper(),
                ),
            ),
            observeIsProUser = { flowOf(isPro) },
            networkConnectivityObserver = { observedConnectivity },
            isConnected = { polledConnectivity.value },
            trackEvent = { name, params -> trackedEvents += name to params },
        )
    }
}
