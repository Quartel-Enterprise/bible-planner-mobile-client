package com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.impl

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.usecase.ObserveStudySuggestionSync
import kotlinx.coroutines.flow.combine

/*
 * Why: writes go through the DataStore-only applySynced* methods so applying a synced
 * value does not push it back again.
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
