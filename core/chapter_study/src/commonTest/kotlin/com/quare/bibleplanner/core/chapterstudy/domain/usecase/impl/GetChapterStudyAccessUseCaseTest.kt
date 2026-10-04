package com.quare.bibleplanner.core.chapterstudy.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyAccessModel
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetChapterStudyAccessUseCaseTest {
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
    private lateinit var useCase: GetChapterStudyAccessUseCase
    private lateinit var repository: FakeChapterStudyRepository
    private lateinit var coordinator: FakeChapterStudyGenerationCoordinator
    private lateinit var prefetchStore: ChapterStudyStatusPrefetchStore
    private var authenticationChecks = 0
    private var proChecks = 0

    @Test
    fun `GIVEN a cached study WHEN checking the access THEN it is open without asking anything else`() = runTest {
        // Given
        prepareScenario(
            cachedStudy = createChapterStudy(),
            userId = null,
            status = status(
                usedCount = 3,
                isUnlocked = false,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
        assertEquals(
            expected = listOf(scopeRequest),
            actual = repository.cacheLookups,
        )
        assertEquals(
            expected = 0,
            actual = authenticationChecks,
        )
        assertEquals(
            expected = 0,
            actual = proChecks,
        )
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN a logged out user WHEN checking the access THEN the login is required`() = runTest {
        // Given
        prepareScenario(
            userId = null,
            isPro = true,
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.LOGIN_REQUIRED,
            actual = access,
        )
        assertEquals(
            expected = 0,
            actual = proChecks,
        )
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN a Pro user WHEN checking the access THEN it is open without a status call`() = runTest {
        // Given
        prepareScenario(
            isPro = true,
            status = status(
                usedCount = 3,
                isUnlocked = false,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
        assertTrue(repository.statusRequests.isEmpty())
    }

    @Test
    fun `GIVEN the status is unavailable WHEN checking the access THEN it is open`() = runTest {
        // Given
        prepareScenario(status = null)

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
        assertEquals(
            expected = listOf(scopeRequest),
            actual = repository.statusRequests,
        )
    }

    @Test
    fun `GIVEN an unlocked chapter and no quota left WHEN checking the access THEN it is open`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 3,
                isUnlocked = true,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
    }

    @Test
    fun `GIVEN fewer studies used than the limit WHEN checking the access THEN it is open`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 2,
                isUnlocked = false,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
    }

    @Test
    fun `GIVEN as many studies used as the limit WHEN checking the access THEN the limit is reached`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 3,
                isUnlocked = false,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.LIMIT_REACHED,
            actual = access,
        )
    }

    @Test
    fun `GIVEN more studies used than the limit WHEN checking the access THEN the limit is reached`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 5,
                isUnlocked = false,
            ),
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.LIMIT_REACHED,
            actual = access,
        )
    }

    @Test
    fun `GIVEN other studies generating fill the quota WHEN checking the access THEN the limit is reached`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 2,
                isUnlocked = false,
            ),
            generatingCount = 1,
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.LIMIT_REACHED,
            actual = access,
        )
        assertEquals(
            expected = listOf(target),
            actual = coordinator.generatingCountExclusions,
        )
    }

    @Test
    fun `GIVEN other studies generating within the quota WHEN checking the access THEN it is open`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 1,
                isUnlocked = false,
            ),
            generatingCount = 1,
        )

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.OPEN,
            actual = access,
        )
    }

    @Test
    fun `GIVEN a prefetched status with quota left WHEN checking the access THEN it is open without a status call`() =
        runTest {
            // Given
            prepareScenario(
                status = status(
                    usedCount = 3,
                    isUnlocked = false,
                ),
                prefetchedStatus = status(
                    usedCount = 2,
                    isUnlocked = false,
                ),
            )

            // When
            val access = useCase(target)

            // Then
            assertEquals(
                expected = ChapterStudyAccessModel.OPEN,
                actual = access,
            )
            assertTrue(repository.statusRequests.isEmpty())
        }

    @Test
    fun `GIVEN a prefetched status without quota left WHEN checking the access THEN the server confirms it`() =
        runTest {
            // Given
            val freshStatus = status(
                usedCount = 2,
                isUnlocked = false,
            )
            prepareScenario(
                status = freshStatus,
                prefetchedStatus = status(
                    usedCount = 3,
                    isUnlocked = false,
                ),
            )

            // When
            val access = useCase(target)

            // Then
            assertEquals(
                expected = ChapterStudyAccessModel.OPEN,
                actual = access,
            )
            assertEquals(
                expected = listOf(scopeRequest),
                actual = repository.statusRequests,
            )
            assertEquals(
                expected = freshStatus,
                actual = prefetchStore.find(statusKey),
            )
        }

    @Test
    fun `GIVEN the prefetched status of another user WHEN checking the access THEN it is ignored`() = runTest {
        // Given
        prepareScenario(
            status = status(
                usedCount = 3,
                isUnlocked = false,
            ),
        )
        prefetchStore.fetchAndKeep(statusKey.copy(userId = "user-2")) {
            status(
                usedCount = 0,
                isUnlocked = false,
            )
        }

        // When
        val access = useCase(target)

        // Then
        assertEquals(
            expected = ChapterStudyAccessModel.LIMIT_REACHED,
            actual = access,
        )
    }

    @Test
    fun `GIVEN a fetched status WHEN checking the access THEN it is kept for the next check`() = runTest {
        // Given
        val fetchedStatus = status(
            usedCount = 3,
            isUnlocked = false,
        )
        prepareScenario(status = fetchedStatus)

        // When
        useCase(target)

        // Then
        assertEquals(
            expected = fetchedStatus,
            actual = prefetchStore.find(statusKey),
        )
    }

    private suspend fun keepInStore(status: ChapterStudyStatusModel) {
        prefetchStore.fetchAndKeep(statusKey) { status }
    }

    private fun status(
        usedCount: Int,
        isUnlocked: Boolean,
    ): ChapterStudyStatusModel = ChapterStudyStatusModel(
        freeLimit = 3,
        usedCount = usedCount,
        isUnlocked = isUnlocked,
        cacheToken = "token",
        rewardedRemainingToday = 2,
    )

    private suspend fun prepareScenario(
        cachedStudy: ChapterStudyModel? = null,
        userId: String? = "user-1",
        isPro: Boolean = false,
        status: ChapterStudyStatusModel? = null,
        generatingCount: Int = 0,
        prefetchedStatus: ChapterStudyStatusModel? = null,
    ) {
        repository = FakeChapterStudyRepository(
            cachedStudy = cachedStudy,
            status = status,
            events = emptyList(),
        )
        coordinator = FakeChapterStudyGenerationCoordinator().apply { this.generatingCount = generatingCount }
        prefetchStore = ChapterStudyStatusPrefetchStore()
        prefetchedStatus?.let { prefetched -> keepInStore(prefetched) }
        useCase = GetChapterStudyAccessUseCase(
            localAccessChecker = ChapterStudyLocalAccessChecker(
                repository = repository,
                scopeResolver = ChapterStudyScopeResolver(
                    bibleRepository = FakeBibleRepository(
                        bibles = emptyList(),
                        selectedVersionId = "ACF",
                    ),
                    getAppLanguageFlow = { flowOf(Language.PORTUGUESE_BRAZIL) },
                    languageCodeMapper = LanguageCodeMapper(),
                ),
                observeAuthenticatedUserId = {
                    flow {
                        authenticationChecks++
                        emit(userId)
                    }
                },
                observeIsProUser = {
                    flow {
                        proChecks++
                        emit(isPro)
                    }
                },
            ),
            quotaChecker = ChapterStudyQuotaChecker(
                repository = repository,
                generationCoordinator = coordinator,
                statusPrefetchStore = prefetchStore,
            ),
        )
    }
}
