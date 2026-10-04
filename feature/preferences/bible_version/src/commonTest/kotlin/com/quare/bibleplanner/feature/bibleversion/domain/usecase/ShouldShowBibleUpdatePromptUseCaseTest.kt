package com.quare.bibleplanner.feature.bibleversion.domain.usecase

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.books.testing.FakeBibleRepository
import com.quare.bibleplanner.core.date.HasCooldownElapsedUseCase
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.domain.BibleUpdatePromptPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

internal class ShouldShowBibleUpdatePromptUseCaseTest {
    private lateinit var useCase: ShouldShowBibleUpdatePromptUseCase

    @Test
    fun `GIVEN pending updates and no previous dismissal WHEN checking THEN shows the prompt`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = null,
        )

        // When
        val shouldShow = useCase()

        // Then
        assertTrue(shouldShow)
    }

    @Test
    fun `GIVEN no pending updates WHEN checking THEN does not show the prompt`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = false,
            lastDismissedAt = null,
        )

        // When
        val shouldShow = useCase()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN a dismissal within the cooldown WHEN checking THEN does not show the prompt`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = NOW - 4.hours.inWholeMilliseconds + 1,
        )

        // When
        val shouldShow = useCase()

        // Then
        assertFalse(shouldShow)
    }

    @Test
    fun `GIVEN a dismissal past the cooldown WHEN checking THEN shows the prompt again`() = runTest {
        // Given
        prepareScenario(
            hasPendingUpdate = true,
            lastDismissedAt = NOW - 4.hours.inWholeMilliseconds,
        )

        // When
        val shouldShow = useCase()

        // Then
        assertTrue(shouldShow)
    }

    private fun prepareScenario(
        hasPendingUpdate: Boolean,
        lastDismissedAt: Long?,
    ) {
        useCase = ShouldShowBibleUpdatePromptUseCase(
            getPendingBibleUpdates = GetPendingBibleUpdatesUseCase(
                bibleRepository = FakeBibleRepository(
                    bibles = listOf(
                        BibleModel(
                            version = VersionModel(
                                id = "ACF",
                                name = "Almeida Corrigida Fiel",
                                version = "1.2.0",
                                language = Language.PORTUGUESE_BRAZIL,
                                chapters = 1189,
                                size = 8245560,
                            ),
                            downloadedChapters = 1189,
                            downloadStatus = DownloadStatusModel.Downloaded,
                            isSelected = false,
                            hasPendingUpdate = hasPendingUpdate,
                        ),
                    ),
                    selectedVersionId = "ACF",
                ),
            ),
            bibleUpdatePromptPreferences = FakeBibleUpdatePromptPreferences(lastDismissedAt),
            hasCooldownElapsed = HasCooldownElapsedUseCase { NOW },
        )
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}

private class FakeBibleUpdatePromptPreferences(
    private val lastDismissedAt: Long?,
) : BibleUpdatePromptPreferences {
    override suspend fun getLastDismissedAt(): Long? = lastDismissedAt

    override suspend fun setLastDismissedAt(timestamp: Long) = error("Unexpected call")
}
