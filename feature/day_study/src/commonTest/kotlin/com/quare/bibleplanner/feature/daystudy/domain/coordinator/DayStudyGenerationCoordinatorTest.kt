package com.quare.bibleplanner.feature.daystudy.domain.coordinator

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.model.HistoricalContextModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.feature.daystudy.domain.usecase.GetDayStudyUseCase
import kotlinx.coroutines.flow.Flow
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
            DayStudyGenerationStatus.Failed(isLimitReached = false),
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

    private fun TestScope.coordinator(repository: FakeDayStudyRepository): DayStudyGenerationCoordinator =
        DayStudyGenerationCoordinator(
            applicationScope = ApplicationScope(this),
            getDayStudy = GetDayStudyUseCase(
                repository = repository,
                bibleRepository = FakeBibleRepository(),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
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
    ) : DayStudyRepository {
        override fun getDayStudy(
            passages: List<PassageModel>,
            version: String,
            languageCode: String,
        ): Flow<DayStudyGenerationEventModel> = flow {
            events.forEach { emit(it) }
            error?.let { throw it }
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
