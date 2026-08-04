package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DismissBibleUpdatePromptUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPendingBibleUpdatesUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.UpdateBibleVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiAction
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class PendingBibleUpdatesViewModel(
    private val getPendingBibleUpdates: GetPendingBibleUpdatesUseCase,
    private val updateBibleVersion: UpdateBibleVersionUseCase,
    private val dismissBibleUpdatePrompt: DismissBibleUpdatePromptUseCase,
    trackEvent: TrackEvent,
) : TrackedViewModel<PendingBibleUpdatesUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<PendingBibleUpdatesUiAction>()
    val uiAction: SharedFlow<PendingBibleUpdatesUiAction> = _uiAction

    private val _pendingVersionNames = MutableStateFlow<List<String>>(emptyList())
    val pendingVersionNames: StateFlow<List<String>> = _pendingVersionNames

    private var pendingVersionIds: List<String> = emptyList()

    init {
        viewModelScope.launch {
            val pendingVersions = getPendingBibleUpdates()
            pendingVersionIds = pendingVersions.map { it.version.id }
            _pendingVersionNames.value = pendingVersions.map { it.version.name }
        }
    }

    override fun handleEvent(event: PendingBibleUpdatesUiEvent) {
        when (event) {
            PendingBibleUpdatesUiEvent.OnUpdateClick -> updatePendingVersions()
            PendingBibleUpdatesUiEvent.OnDismissClick -> dismiss()
        }
    }

    private fun updatePendingVersions() {
        viewModelScope.launch {
            pendingVersionIds.forEach { versionId ->
                updateBibleVersion(versionId)
            }
            _uiAction.emit(PendingBibleUpdatesUiAction.NavigateBack)
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            dismissBibleUpdatePrompt()
            _uiAction.emit(PendingBibleUpdatesUiAction.NavigateBack)
        }
    }
}
