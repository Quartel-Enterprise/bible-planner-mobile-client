package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.testing.ChapterStudyRequest
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.chapterstudy.testing.createChapterStudy
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FindCachedChapterStudyUseCaseTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.JHN,
        chapterNumber = 3,
    )
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var useCase: FindCachedChapterStudyUseCase

    @Test
    fun `GIVEN a cached study WHEN finding it THEN returns it`() = runTest {
        // Given
        prepareScenario(cachedStudy = createChapterStudy())

        // When
        val study = useCase(target)

        // Then
        assertEquals(
            expected = createChapterStudy(),
            actual = study,
        )
    }

    @Test
    fun `GIVEN no cached study WHEN finding it THEN returns null`() = runTest {
        // Given
        prepareScenario(cachedStudy = null)

        // When
        val study = useCase(target)

        // Then
        assertNull(study)
    }

    @Test
    fun `GIVEN no cached study WHEN finding a study THEN looks it up for the selected version and the app language`() =
        runTest {
            // Given
            prepareScenario(cachedStudy = null)

            // When
            useCase(target)

            // Then
            assertEquals(
                expected = listOf(
                    ChapterStudyRequest(
                        chapter = ChapterRef(
                            bibleVersionId = "KJV",
                            bookId = BookId.JHN,
                            chapterNumber = 3,
                        ),
                        languageCode = "en",
                    ),
                ),
                actual = repository.cacheLookups,
            )
        }

    private fun prepareScenario(cachedStudy: ChapterStudyModel?) {
        repository = FakeChapterStudyRepository(
            cachedStudy = cachedStudy,
            status = null,
            events = emptyList(),
        )
        useCase = FindCachedChapterStudyUseCase(
            repository = repository,
            scopeResolver = ChapterStudyScopeResolver(
                bibleRepository = FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "KJV",
                ),
                getAppLanguageFlow = { flowOf(Language.ENGLISH) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
        )
    }
}
