package com.quare.bibleplanner.core.daystudy.domain.usecase

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyPhaseModel
import com.quare.bibleplanner.core.daystudy.testing.DayStudyRequest
import com.quare.bibleplanner.core.daystudy.testing.FakeDayStudyRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class GetDayStudyUseCaseTest {
    private lateinit var dayStudyRepository: FakeDayStudyRepository
    private lateinit var useCase: GetDayStudyUseCase

    @BeforeTest
    fun setUp() {
        dayStudyRepository = FakeDayStudyRepository(
            hasCached = false,
            status = null,
            statusError = null,
            events = emptyList(),
        )
        useCase = GetDayStudyUseCase(
            repository = dayStudyRepository,
            bibleRepository = FakeBibleRepository(
                bibles = emptyList(),
                selectedVersionId = "ACF",
            ),
            getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
            languageCodeMapper = LanguageCodeMapper(),
        )
    }

    @Test
    fun `GIVEN a selected version and app language WHEN invoking THEN forwards them mapped to the repository`() =
        runTest {
            // When
            useCase(
                passages = passages,
                isRewarded = false,
            ).toList()

            // Then
            assertEquals(
                listOf(
                    DayStudyRequest(
                        passages = passages,
                        version = "ACF",
                        languageCode = "pt-BR",
                    ),
                ),
                dayStudyRepository.studyRequests,
            )
        }

    @Test
    fun `GIVEN a rewarded unlock WHEN invoking THEN asks the repository for a rewarded study`() = runTest {
        // When
        useCase(
            passages = passages,
            isRewarded = true,
        ).toList()

        // Then
        assertEquals(listOf(true), dayStudyRepository.studyRewardFlags)
    }

    @Test
    fun `GIVEN repository events WHEN invoking THEN emits them`() = runTest {
        // Given
        dayStudyRepository.events = listOf(
            DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.READING),
            DayStudyGenerationEventModel.PhaseChanged(DayStudyPhaseModel.CHAPTERS),
        )

        // When
        val emitted = useCase(
            passages = passages,
            isRewarded = false,
        ).toList()

        // Then
        assertEquals(dayStudyRepository.events, emitted)
    }

    @Test
    fun `GIVEN a failing repository stream WHEN invoking THEN the failure propagates`() = runTest {
        // Given
        dayStudyRepository.eventsError = LimitReachedException()

        // When
        val result = runCatching {
            useCase(
                passages = passages,
                isRewarded = false,
            ).toList()
        }

        // Then
        assertIs<LimitReachedException>(result.exceptionOrNull())
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
}
