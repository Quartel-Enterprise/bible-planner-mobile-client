package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import androidx.datastore.preferences.core.emptyPreferences
import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterListeningUnlockRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningAccessModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.datastore.testing.FakePreferencesDataStore
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetChapterListeningAccessUseCaseTest {
    private val today = LocalDate(2026, 10, 4)
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private lateinit var useCase: GetChapterListeningAccessUseCase
    private lateinit var unlockRepository: ChapterListeningUnlockRepositoryImpl
    private lateinit var recordUnlock: RecordChapterListeningUnlockUseCase

    @Test
    fun `GIVEN a Pro user WHEN asking for a chapter THEN it is open`() = runTest {
        // Given
        prepareScenario(isPro = true)

        // When
        val access = useCase(genesisOne)

        // Then
        assertEquals(ChapterListeningAccessModel.Open, access)
    }

    @Test
    fun `GIVEN a free user with the daily unlock left WHEN asking THEN offers the unlock`() = runTest {
        // Given
        prepareScenario(isPro = false)

        // When
        val access = useCase(genesisOne)

        // Then
        assertEquals(ChapterListeningAccessModel.UnlockAvailable(1), access)
    }

    @Test
    fun `GIVEN a chapter unlocked today WHEN asking for it again THEN it is open`() = runTest {
        // Given
        prepareScenario(isPro = false)
        recordUnlock(genesisOne)

        // When
        val access = useCase(genesisOne)

        // Then
        assertEquals(ChapterListeningAccessModel.Open, access)
    }

    @Test
    fun `GIVEN the daily unlock used WHEN asking for another chapter THEN the limit is reached`() = runTest {
        // Given
        prepareScenario(isPro = false)
        recordUnlock(genesisOne)

        // When
        val access = useCase(genesisTwo)

        // Then
        assertEquals(ChapterListeningAccessModel.LimitReached, access)
    }

    @Test
    fun `GIVEN a higher remote limit WHEN asking after one unlock THEN offers what is left`() = runTest {
        // Given
        prepareScenario(
            isPro = false,
            dailyLimit = 3,
        )
        recordUnlock(genesisOne)

        // When
        val access = useCase(genesisTwo)

        // Then
        assertEquals(ChapterListeningAccessModel.UnlockAvailable(2), access)
    }

    private fun prepareScenario(
        isPro: Boolean,
        dailyLimit: Int? = null,
    ) {
        val dateProvider = ListeningDateProvider(
            currentTimestampProvider = { 0L },
            localDateTimeProvider = { LocalDateTime(today, LocalTime(9, 0)) },
        )
        unlockRepository = ChapterListeningUnlockRepositoryImpl(FakePreferencesDataStore(emptyPreferences()))
        recordUnlock = RecordChapterListeningUnlockUseCase(
            unlockRepository = unlockRepository,
            dateProvider = dateProvider,
        )
        useCase = GetChapterListeningAccessUseCase(
            isProUser = { isPro },
            unlockRepository = unlockRepository,
            dateProvider = dateProvider,
            getIntRemoteConfig = FakeGetIntRemoteConfig(dailyLimit),
        )
    }
}

private class FakeGetIntRemoteConfig(
    private val value: Int?,
) : GetIntRemoteConfig {
    override suspend fun invoke(
        key: String,
        default: Int,
    ): Int = value ?: default
}
