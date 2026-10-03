package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyQuotaModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.testing.ChapterStudyRequest
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyGenerationCoordinator
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterRef
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GetChapterStudyQuotaUseCaseTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private lateinit var useCase: GetChapterStudyQuotaUseCase
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var coordinator: FakeChapterStudyGenerationCoordinator

    @Test
    fun `GIVEN a logged out user WHEN reading the quota THEN has none to show without asking for it`() = runTest {
        // Given
        prepareScenario(
            userId = null,
            status = status(usedCount = 1),
        )

        // When
        val quota = useCase(target)

        // Then
        assertNull(quota)
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN no status WHEN reading the quota THEN has none to show`() = runTest {
        // Given
        prepareScenario(status = null)

        // When
        val quota = useCase(target)

        // Then
        assertNull(quota)
    }

    @Test
    fun `GIVEN studies used and generating WHEN reading the quota THEN counts both as spent`() = runTest {
        // Given
        prepareScenario(
            status = status(usedCount = 1),
            generatingCount = 1,
        )

        // When
        val quota = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyQuotaModel(
                freeLimit = 3,
                remainingFree = 1,
                isUnlocked = false,
            ),
            actual = quota,
        )
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
            actual = repository.statusRequests,
        )
        assertEquals(expected = listOf(target), actual = coordinator.generatingCountExclusions)
    }

    @Test
    fun `GIVEN more studies spent than the limit WHEN reading the quota THEN has none left`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 3,
                isUnlocked = true,
            ),
            generatingCount = 2,
        )

        // When
        val quota = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyQuotaModel(
                freeLimit = 3,
                remainingFree = 0,
                isUnlocked = true,
            ),
            actual = quota,
        )
    }

    private fun status(
        usedCount: Int,
        isUnlocked: Boolean = false,
    ): ChapterStudyStatusModel = ChapterStudyStatusModel(
        freeLimit = 3,
        usedCount = usedCount,
        isUnlocked = isUnlocked,
        cacheToken = "token",
    )

    private fun prepareScenario(
        userId: String? = "user-1",
        status: ChapterStudyStatusModel?,
        generatingCount: Int = 0,
    ) {
        repository = FakeChapterStudyRepository(
            cachedStudy = null,
            status = status,
            events = emptyList(),
        )
        coordinator = FakeChapterStudyGenerationCoordinator().apply { this.generatingCount = generatingCount }
        useCase = GetChapterStudyQuotaUseCase(
            repository = repository,
            scopeResolver = ChapterStudyScopeResolver(
                bibleRepository = FakeBibleRepository(
                    bibles = emptyList(),
                    selectedVersionId = "ACF",
                ),
                getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                languageCodeMapper = LanguageCodeMapper(),
            ),
            generationCoordinator = coordinator,
            observeAuthenticatedUserId = { flowOf(userId) },
        )
    }
}
