package com.quare.bibleplanner.feature.daystudy.presentation.factory

import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.daystudy.domain.mapper.LanguageCodeMapper
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.usecase.GetDayStudyQuotaUseCase
import com.quare.bibleplanner.core.daystudy.domain.usecase.HasCachedStudyUseCase
import com.quare.bibleplanner.core.daystudy.testing.FakeDayStudyRepository
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.daystudy.fake.DefaultIntRemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DayStudyCardLoaderTest {
    @Test
    fun `GIVEN the passages changed after observing WHEN the Pro state changes THEN reloads the new passages`() =
        runTest(UnconfinedTestDispatcher()) {
            // Given
            val repository = FakeDayStudyRepository(
                hasCached = false,
                status = DayStudyStatusModel(
                    freeLimit = 3,
                    usedCount = 0,
                    isUnlocked = false,
                    cacheToken = "token",
                    rewardedRemainingToday = 2,
                ),
                statusError = null,
                events = emptyList(),
            )
            val bibleRepository = FakeBibleRepository(
                bibles = emptyList(),
                selectedVersionId = "ACF",
            )
            val isPro = MutableStateFlow(false)
            val loader = DayStudyCardLoader(
                getDayStudyQuota = GetDayStudyQuotaUseCase(
                    repository = repository,
                    bibleRepository = bibleRepository,
                    getAppLanguageFlow = { flowOf(Language.ENGLISH) },
                    languageCodeMapper = LanguageCodeMapper(),
                    getIntRemoteConfig = DefaultIntRemoteConfig(),
                ),
                hasCachedStudy = HasCachedStudyUseCase(
                    repository = repository,
                    bibleRepository = bibleRepository,
                    getAppLanguageFlow = { flowOf(Language.ENGLISH) },
                    languageCodeMapper = LanguageCodeMapper(),
                ),
                observeIsProUser = { isPro },
                observeAuthenticatedUserId = { flowOf("user-id") },
                cardUiModelFactory = DayStudyCardUiModelFactory(prepareRewardedUnlockOffer = { false }),
                trackEvent = { _, _ -> },
            )
            var passages = listOf(passageOf(BookId.GEN))
            backgroundScope.launch {
                loader
                    .observe(
                        passages = { passages },
                        useCache = false,
                    ).collect {}
            }

            // When
            passages = listOf(passageOf(BookId.EXO))
            isPro.value = true

            // Then
            assertEquals(passages, repository.statusRequests.last())
            assertEquals(true, loader.isPro)
        }

    private fun passageOf(bookId: BookId): PassageModel = PassageModel(
        bookId = bookId,
        chapters = listOf(
            ChapterModel(
                number = 1,
                startVerse = null,
                endVerse = null,
                bookId = bookId,
            ),
        ),
        isRead = false,
        chapterRanges = "1",
    )
}
