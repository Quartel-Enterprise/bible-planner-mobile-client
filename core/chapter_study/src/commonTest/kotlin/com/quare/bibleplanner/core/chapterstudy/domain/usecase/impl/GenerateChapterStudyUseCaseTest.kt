package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyPhaseModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.testing.ChapterStudyRequest
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class GenerateChapterStudyUseCaseTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var useCase: GenerateChapterStudyUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeChapterStudyRepository(
            cachedStudy = null,
            status = null,
            events = emptyList(),
        )
        useCase = GenerateChapterStudyUseCase(
            repository = repository,
            scopeResolver = ChapterStudyScopeResolver(
                bibleRepository = FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "ACF",
                ),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
        )
    }

    @Test
    fun `GIVEN the selected version and the app language WHEN generating THEN asks the repository for both`() =
        runTest {
            // When
            useCase(
                target = target,
                isRewarded = false,
            ).toList()

            // Then
            assertEquals(
                expected = listOf(
                    ChapterStudyRequest(
                        chapter = ChapterRef(
                            bibleVersionId = "ACF",
                            bookId = BookId.GEN,
                            chapterNumber = 3,
                        ),
                        languageCode = "pt-BR",
                    ),
                ),
                actual = repository.generationRequests,
            )
        }

    @Test
    fun `GIVEN a rewarded unlock WHEN generating THEN asks the repository for a rewarded study`() = runTest {
        // When
        useCase(
            target = target,
            isRewarded = true,
        ).toList()

        // Then
        assertEquals(
            expected = listOf(true),
            actual = repository.generationRewardFlags,
        )
    }

    @Test
    fun `GIVEN repository events WHEN generating THEN emits them in order`() = runTest {
        // Given
        repository.events = listOf(
            ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.READING),
            ChapterStudyGenerationEventModel.PhaseChanged(ChapterStudyPhaseModel.SUMMARY),
            ChapterStudyGenerationEventModel.Completed(createChapterStudy()),
        )

        // When
        val emitted = useCase(
            target = target,
            isRewarded = false,
        ).toList()

        // Then
        assertEquals(
            expected = repository.events,
            actual = emitted,
        )
    }

    @Test
    fun `GIVEN a failing repository stream WHEN generating THEN the failure propagates`() = runTest {
        // Given
        repository.eventsError = LimitReachedException()

        // When
        val result = runCatching {
            useCase(
                target = target,
                isRewarded = false,
            ).toList()
        }

        // Then
        assertIs<LimitReachedException>(result.exceptionOrNull())
    }
}
