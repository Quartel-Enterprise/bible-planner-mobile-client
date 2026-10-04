package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.testing.ChapterStudyRequest
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RefreshChapterStudyCacheUseCaseTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.PSA,
        chapterNumber = 23,
    )
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var useCase: RefreshChapterStudyCacheUseCase

    @Test
    fun `GIVEN the selected version and the app language WHEN refreshing THEN asks the status for both`() = runTest {
        // Given
        prepareScenario(
            status = ChapterStudyStatusModel(
                freeLimit = 3,
                usedCount = 1,
                isUnlocked = true,
                cacheToken = "token",
                rewardedRemainingToday = 2,
            ),
        )

        // When
        useCase(target)

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyRequest(
                    chapter = ChapterRef(
                        bibleVersionId = "RVR",
                        bookId = BookId.PSA,
                        chapterNumber = 23,
                    ),
                    languageCode = "es",
                ),
            ),
            actual = repository.statusRequests,
        )
    }

    @Test
    fun `GIVEN the status is unavailable WHEN refreshing THEN completes without touching the cache`() = runTest {
        // Given
        prepareScenario(status = null)

        // When
        useCase(target)

        // Then
        assertEquals(
            expected = 1,
            actual = repository.statusRequests.size,
        )
        assertTrue(repository.cacheLookups.isEmpty())
        assertEquals(
            expected = 0,
            actual = repository.cacheClearCount,
        )
    }

    private fun prepareScenario(status: ChapterStudyStatusModel?) {
        repository = FakeChapterStudyRepository(
            cachedStudy = null,
            status = status,
            events = emptyList(),
        )
        useCase = RefreshChapterStudyCacheUseCase(
            repository = repository,
            scopeResolver = ChapterStudyScopeResolver(
                bibleRepository = FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "RVR",
                ),
                getAppLanguageFlow = { flowOf(Language.SPANISH) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
        )
    }
}
