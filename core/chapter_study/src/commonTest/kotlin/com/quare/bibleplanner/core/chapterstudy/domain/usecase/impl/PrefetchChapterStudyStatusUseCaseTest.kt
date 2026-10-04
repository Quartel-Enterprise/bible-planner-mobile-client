package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyTargetModel
import com.quare.bibleplanner.core.chapterstudy.domain.store.ChapterStudyStatusPrefetchStore
import com.quare.bibleplanner.core.chapterstudy.testing.ChapterStudyRequest
import com.quare.bibleplanner.core.chapterstudy.testing.FakeChapterStudyGenerationCoordinator
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
import kotlin.test.assertTrue

internal class PrefetchChapterStudyStatusUseCaseTest {
    private val target = ChapterStudyTargetModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val scopeRequest = ChapterStudyRequest(
        chapter = ChapterRef(
            bibleVersionId = "ACF",
            bookId = BookId.GEN,
            chapterNumber = 3,
        ),
        languageCode = "pt-BR",
    )
    private val statusKey = ChapterStudyStatusKey(
        userId = "user-1",
        scope = ChapterStudyScope(
            chapter = scopeRequest.chapter,
            languageCode = scopeRequest.languageCode,
        ),
    )
    private val status = ChapterStudyStatusModel(
        freeLimit = 3,
        usedCount = 1,
        isUnlocked = false,
        cacheToken = "token",
        rewardedRemainingToday = 2,
    )
    private lateinit var useCase: PrefetchChapterStudyStatusUseCase
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var store: ChapterStudyStatusPrefetchStore

    @Test
    fun `GIVEN a free user without the study WHEN prefetching THEN stores the chapter status`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = false,
            status = status,
        )

        // When
        useCase(target)

        // Then
        assertEquals(
            expected = listOf(scopeRequest),
            actual = repository.statusRequests,
        )
        assertEquals(
            expected = status,
            actual = store.find(statusKey),
        )
    }

    @Test
    fun `GIVEN a stored status with quota left WHEN prefetching THEN asks the server nothing`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = false,
            status = status.copy(usedCount = 3),
        )
        store.fetchAndKeep(statusKey) { status }

        // When
        useCase(target)

        // Then
        assertTrue(repository.statusRequests.isEmpty())
        assertEquals(
            expected = status,
            actual = store.find(statusKey),
        )
    }

    @Test
    fun `GIVEN a stored status without quota left WHEN prefetching THEN refreshes it`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = false,
            status = status,
        )
        store.fetchAndKeep(statusKey) { status.copy(usedCount = 3) }

        // When
        useCase(target)

        // Then
        assertEquals(
            expected = listOf(scopeRequest),
            actual = repository.statusRequests,
        )
        assertEquals(
            expected = status,
            actual = store.find(statusKey),
        )
    }

    @Test
    fun `GIVEN the study is already cached WHEN prefetching THEN asks the server nothing`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = createChapterStudy(),
            userId = "user-1",
            isPro = false,
            status = status,
        )

        // When
        useCase(target)

        // Then
        assertTrue(repository.statusRequests.isEmpty())
        assertNull(store.find(statusKey))
    }

    @Test
    fun `GIVEN a logged out user WHEN prefetching THEN asks the server nothing`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = null,
            isPro = false,
            status = status,
        )

        // When
        useCase(target)

        // Then
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN a Pro user WHEN prefetching THEN asks the server nothing`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = true,
            status = status,
        )

        // When
        useCase(target)

        // Then
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN the status is unavailable WHEN prefetching THEN stores nothing`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = false,
            status = null,
        )

        // When
        useCase(target)

        // Then
        assertNull(store.find(statusKey))
    }

    @Test
    fun `GIVEN the language cannot be read WHEN prefetching THEN swallows the failure`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = null,
            userId = "user-1",
            isPro = false,
            status = status,
            languageError = IllegalStateException("broken"),
        )

        // When
        useCase(target)

        // Then
        assertTrue(repository.statusRequests.isEmpty())
        assertNull(store.find(statusKey))
    }

    private fun prepareScenario(
        cachedStudy: ChapterStudyModel?,
        userId: String?,
        isPro: Boolean,
        status: ChapterStudyStatusModel?,
        languageError: Throwable? = null,
    ) {
        repository = FakeChapterStudyRepository(
            cachedStudy = cachedStudy,
            status = status,
            events = emptyList(),
        )
        store = ChapterStudyStatusPrefetchStore()
        useCase = PrefetchChapterStudyStatusUseCase(
            localAccessChecker = ChapterStudyLocalAccessChecker(
                repository = repository,
                scopeResolver = ChapterStudyScopeResolver(
                    bibleRepository = FakeBibleRepository(
                        bibles = emptyList(),
                        selectedVersionId = "ACF",
                    ),
                    getAppLanguageFlow = {
                        languageError?.let { error -> throw error }
                        flowOf(Language.PORTUGUESE_BRAZIL)
                    },
                    languageCodeMapper = LanguageCodeMapper(),
                ),
                observeAuthenticatedUserId = { flowOf(userId) },
                observeIsProUser = { flowOf(isPro) },
            ),
            quotaChecker = ChapterStudyQuotaChecker(
                repository = repository,
                generationCoordinator = FakeChapterStudyGenerationCoordinator(),
                statusPrefetchStore = store,
            ),
        )
    }
}
