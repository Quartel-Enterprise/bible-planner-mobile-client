package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObserveStudySuggestionSyncUseCaseTest {
    @Test
    fun `GIVEN sync enabled with synced values WHEN observing THEN applies them`() = runTest {
        // Given
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = true,
            syncedEnabled = false,
            syncedMode = StudySuggestionMode.BANNER,
        )

        // When
        ObserveStudySuggestionSyncUseCase(repository).invoke()

        // Then
        assertEquals(false, repository.appliedEnabled)
        assertEquals(StudySuggestionMode.BANNER, repository.appliedMode)
    }

    @Test
    fun `GIVEN sync disabled WHEN observing THEN applies nothing`() = runTest {
        // Given
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = false,
            syncedEnabled = false,
            syncedMode = StudySuggestionMode.BANNER,
        )

        // When
        ObserveStudySuggestionSyncUseCase(repository).invoke()

        // Then
        assertNull(repository.appliedEnabled)
        assertNull(repository.appliedMode)
    }

    @Test
    fun `GIVEN sync enabled without synced values WHEN observing THEN skips them`() = runTest {
        // Given
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = true,
            syncedEnabled = null,
            syncedMode = null,
        )

        // When
        ObserveStudySuggestionSyncUseCase(repository).invoke()

        // Then
        assertNull(repository.appliedEnabled)
        assertNull(repository.appliedMode)
    }

    private class FakeStudySuggestionSettingsRepository(
        private val syncEnabled: Boolean,
        private val syncedEnabled: Boolean?,
        private val syncedMode: StudySuggestionMode?,
    ) : StudySuggestionSettingsRepository {
        var appliedEnabled: Boolean? = null
        var appliedMode: StudySuggestionMode? = null

        override fun getSyncEnabledFlow(): Flow<Boolean> = flowOf(syncEnabled)

        override fun observeSyncedEnabled(): Flow<Boolean?> = flowOf(syncedEnabled)

        override fun observeSyncedMode(): Flow<StudySuggestionMode?> = flowOf(syncedMode)

        override suspend fun applySyncedEnabled(isEnabled: Boolean) {
            appliedEnabled = isEnabled
        }

        override suspend fun applySyncedMode(mode: StudySuggestionMode) {
            appliedMode = mode
        }

        override fun observe(): Flow<StudySuggestionSettingsModel> = flowOf(
            StudySuggestionSettingsModel(
                isEnabled = true,
                mode = StudySuggestionMode.DIALOG,
            ),
        )

        override suspend fun setEnabled(isEnabled: Boolean) {}

        override suspend fun setMode(mode: StudySuggestionMode) {}

        override suspend fun setSyncEnabled(enabled: Boolean) {}
    }
}
