package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.domain.repository.BibleRepository
import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

internal class ShouldShowBibleUpdatePromptUseCaseTest {
    private lateinit var useCase: ShouldShowBibleUpdatePromptUseCase

    @Test
    fun `shows the prompt when there are pending updates and no previous dismissal`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = null,
        )

        // When / Then
        assertTrue(useCase())
    }

    @Test
    fun `does not show the prompt when there are no pending updates`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = false,
            lastDismissedAt = null,
        )

        // When / Then
        assertFalse(useCase())
    }

    @Test
    fun `does not show the prompt within the dismissal cooldown`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = NOW - 4.hours.inWholeMilliseconds + 1,
        )

        // When / Then
        assertFalse(useCase())
    }

    @Test
    fun `shows the prompt again after the dismissal cooldown`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = NOW - 4.hours.inWholeMilliseconds,
        )

        // When / Then
        assertTrue(useCase())
    }

    private fun prepareScenario(
        hasPendingUpdate: Boolean,
        lastDismissedAt: Long?,
    ) {
        useCase = ShouldShowBibleUpdatePromptUseCase(
            getPendingBibleUpdates = GetPendingBibleUpdatesUseCase(
                bibleRepository = FakeBibleRepository(hasPendingUpdate),
            ),
            bibleUpdatePromptPreferences = FakeBibleUpdatePromptPreferences(lastDismissedAt),
            hasCooldownElapsed = HasCooldownElapsedUseCase { NOW },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}

private class FakeBibleRepository(
    private val hasPendingUpdate: Boolean,
) : BibleRepository {
    override fun getBiblesFlow(): Flow<List<BibleModel>> = flowOf(
        listOf(
            BibleModel(
                version = VersionModel(
                    id = "ACF",
                    name = "Almeida Corrigida Fiel",
                    version = "1.2.0",
                    language = Language.PORTUGUESE_BRAZIL,
                    chapters = 1189,
                    size = 8245560,
                ),
                downloadStatus = DownloadStatusModel.Downloaded,
                isSelected = false,
                hasPendingUpdate = hasPendingUpdate,
            ),
        ),
    )

    override fun getSelectedVersionIdFlow(): Flow<String> = error("Unexpected call")

    override suspend fun setSelectedVersionId(id: String) = error("Unexpected call")
}

private class FakeBibleUpdatePromptPreferences(
    private val lastDismissedAt: Long?,
) : BibleUpdatePromptPreferences {
    override suspend fun getLastDismissedAt(): Long? = lastDismissedAt

    override suspend fun setLastDismissedAt(timestamp: Long) = error("Unexpected call")
}
