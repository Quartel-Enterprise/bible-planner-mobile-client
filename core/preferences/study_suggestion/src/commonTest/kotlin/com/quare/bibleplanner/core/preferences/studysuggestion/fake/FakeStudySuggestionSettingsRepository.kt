package com.quare.bibleplanner.core.preferences.studysuggestion.fake

import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionMode
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.model.StudySuggestionSettingsModel
import com.quare.bibleplanner.core.preferences.studysuggestion.domain.repository.StudySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeStudySuggestionSettingsRepository(
    settings: StudySuggestionSettingsModel,
    isSyncEnabled: Boolean,
) : StudySuggestionSettingsRepository {
    val settings = MutableStateFlow(settings)
    val isSyncEnabled = MutableStateFlow(isSyncEnabled)

    override fun observe(): Flow<StudySuggestionSettingsModel> = settings

    override suspend fun setEnabled(isEnabled: Boolean) {
        settings.value = settings.value.copy(isEnabled = isEnabled)
    }

    override suspend fun setMode(mode: StudySuggestionMode) {
        settings.value = settings.value.copy(mode = mode)
    }

    override fun getSyncEnabledFlow(): Flow<Boolean> = isSyncEnabled

    override suspend fun setSyncEnabled(enabled: Boolean) {
        isSyncEnabled.value = enabled
    }

    override fun observeSyncedEnabled(): Flow<Boolean?> = error("unused")

    override fun observeSyncedMode(): Flow<StudySuggestionMode?> = error("unused")

    override suspend fun applySyncedEnabled(isEnabled: Boolean) = error("unused")

    override suspend fun applySyncedMode(mode: StudySuggestionMode) = error("unused")
}
