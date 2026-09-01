package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObserveStudySuggestionSyncUseCaseTest {
    @Test
    fun `applies synced values when sync is enabled`() = runTest {
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = true,
            syncedEnabled = false,
            syncedMode = StudySuggestionMode.BANNER,
        )

        ObserveStudySuggestionSyncUseCase(repository).invoke()

        assertEquals(false, repository.appliedEnabled)
        assertEquals(StudySuggestionMode.BANNER, repository.appliedMode)
    }

    @Test
    fun `applies nothing when sync is disabled`() = runTest {
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = false,
            syncedEnabled = false,
            syncedMode = StudySuggestionMode.BANNER,
        )

        ObserveStudySuggestionSyncUseCase(repository).invoke()

        assertNull(repository.appliedEnabled)
        assertNull(repository.appliedMode)
    }

    @Test
    fun `skips missing synced values`() = runTest {
        val repository = FakeStudySuggestionSettingsRepository(
            syncEnabled = true,
            syncedEnabled = null,
            syncedMode = null,
        )

        ObserveStudySuggestionSyncUseCase(repository).invoke()

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

        override suspend fun setEnabled(isEnabled: Boolean) = Unit

        override suspend fun setMode(mode: StudySuggestionMode) = Unit

        override suspend fun setSyncEnabled(enabled: Boolean) = Unit
    }
}
