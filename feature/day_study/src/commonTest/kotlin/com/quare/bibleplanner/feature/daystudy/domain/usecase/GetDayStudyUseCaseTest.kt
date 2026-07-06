package com.quare.bibleplanner.feature.daystudy.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.feature.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.feature.daystudy.domain.repository.DayStudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class GetDayStudyUseCaseTest {
    private lateinit var dayStudyRepository: FakeDayStudyRepository
    private lateinit var useCase: GetDayStudyUseCase

    @BeforeTest
    fun setUp() {
        dayStudyRepository = FakeDayStudyRepository()
        useCase = GetDayStudyUseCase(
            repository = dayStudyRepository,
            bibleRepository = FakeBibleRepository(),
            getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
            languageCodeMapper = LanguageCodeMapper(),
        )
    }

    @Test
    fun `WHEN invoking THEN forwards the selected version and mapped language to the repository`() = runTest {
        // When
        useCase(passages).toList()

        // Then
        assertEquals("ACF", dayStudyRepository.receivedVersion)
        assertEquals("pt-BR", dayStudyRepository.receivedLanguageCode)
        assertEquals(passages, dayStudyRepository.receivedPassages)
    }

    @Test
    fun `WHEN invoking THEN emits the repository events`() = runTest {
        // Given
        dayStudyRepository.events = listOf(
            DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
            DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
        )

        // When
        val emitted = useCase(passages).toList()

        // Then
        assertEquals(dayStudyRepository.events, emitted)
    }

    @Test
    fun `GIVEN a failing repository stream WHEN invoking THEN the failure propagates`() = runTest {
        // Given
        dayStudyRepository.error = LimitReachedException()

        // When & Then
        assertFailsWith<LimitReachedException> {
            useCase(passages).toList()
        }
    }

    private val passages = listOf(
        PassageModel(
            bookId = BookId.GEN,
            chapters = listOf(
                ChapterModel(
                    number = 1,
                    startVerse = null,
                    endVerse = null,
                    bookId = BookId.GEN,
                ),
            ),
            isRead = false,
            chapterRanges = null,
        ),
    )

    private class FakeDayStudyRepository : DayStudyRepository {
        var receivedPassages: List<PassageModel>? = null
        var receivedVersion: String? = null
        var receivedLanguageCode: String? = null
        var events: List<DayStudyGenerationEventModel> = emptyList()
        var error: Throwable? = null

        override fun getDayStudy(
            passages: List<PassageModel>,
            version: String,
            languageCode: String,
        ): Flow<DayStudyGenerationEventModel> = flow {
            receivedPassages = passages
            receivedVersion = version
            receivedLanguageCode = languageCode
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
