package com.quare.bibleplanner.feature.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.feature.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.feature.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.feature.studysuggestion.domain.usecase.ObserveStudySuggestionSync
import kotlinx.coroutines.flow.combine

/**
 * App-scoped collector that applies synced study-suggestion values into the device-local store while
 * the sync flag is on. Writing through the `applySynced*` methods (DataStore-only) avoids re-pushing
 * the value.
 */
internal class ObserveStudySuggestionSyncUseCase(
    private val repository: StudySuggestionSettingsRepository,
) : ObserveStudySuggestionSync {
    override suspend fun invoke() {
        combine(
            repository.getSyncEnabledFlow(),
            repository.observeSyncedEnabled(),
            repository.observeSyncedMode(),
        ) { isSyncEnabled, isEnabled, mode ->
            SyncedValues(
                isSyncEnabled = isSyncEnabled,
                isEnabled = isEnabled,
                mode = mode,
            )
        }.collect { values ->
            if (!values.isSyncEnabled) return@collect
            values.isEnabled?.let { repository.applySyncedEnabled(it) }
            values.mode?.let { repository.applySyncedMode(it) }
        }
    }

    private data class SyncedValues(
        val isSyncEnabled: Boolean,
        val isEnabled: Boolean?,
        val mode: StudySuggestionMode?,
    )
}
