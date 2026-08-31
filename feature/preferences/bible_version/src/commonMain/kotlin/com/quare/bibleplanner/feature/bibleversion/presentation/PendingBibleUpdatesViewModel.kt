package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestDownloadNotificationPermissionUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.DismissBibleUpdatePromptUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.GetPendingBibleUpdatesUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.UpdateBibleVersionUseCase
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdateItem
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PendingBibleUpdatesViewModel(
    private val getPendingBibleUpdates: GetPendingBibleUpdatesUseCase,
    private val updateBibleVersion: UpdateBibleVersionUseCase,
    private val dismissBibleUpdatePrompt: DismissBibleUpdatePromptUseCase,
    private val requestDownloadNotificationPermission: RequestDownloadNotificationPermissionUseCase,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<PendingBibleUpdatesUiEvent>(trackEvent) {
    private val _pendingUpdates = MutableStateFlow<List<PendingBibleUpdateItem>>(emptyList())
    val pendingUpdates: StateFlow<List<PendingBibleUpdateItem>> = _pendingUpdates

    init {
        viewModelScope.launch {
            _pendingUpdates.value = getPendingBibleUpdates().map { bible ->
                PendingBibleUpdateItem(
                    id = bible.version.id,
                    name = bible.version.name,
                    size = bible.version.size,
                    isSelected = true,
                )
            }
        }
    }

    override fun handleEvent(event: PendingBibleUpdatesUiEvent) {
        when (event) {
            is PendingBibleUpdatesUiEvent.OnToggleVersion -> toggleVersion(event.id)
            PendingBibleUpdatesUiEvent.OnUpdateClick -> updateSelectedVersions()
            PendingBibleUpdatesUiEvent.OnDismissClick -> dismiss()
        }
    }

    private fun toggleVersion(id: String) {
        _pendingUpdates.update { items ->
            items.map { item ->
                if (item.id == id) item.copy(isSelected = !item.isSelected) else item
            }
        }
        val isSelected = _pendingUpdates.value.first { it.id == id }.isSelected
        trackEvent(
            name = AnalyticsEventNames.BIBLE_VERSION_UPDATE_PROMPT_VERSION_TOGGLED,
            params = mapOf(
                AnalyticsParams.VERSION_ID to id,
                AnalyticsParams.IS_SELECTED to isSelected,
            ),
        )
    }

    private fun updateSelectedVersions() {
        viewModelScope.launch {
            val selectedUpdates = _pendingUpdates.value.filter { it.isSelected }
            selectedUpdates.forEach { item ->
                updateBibleVersion(item.id)
            }
            if (selectedUpdates.isNotEmpty()) {
                requestDownloadNotificationPermission()
            }
            navigator.navigateBack()
        }
    }

    private fun dismiss() {
        viewModelScope.launch {
            dismissBibleUpdatePrompt()
            navigator.navigateBack()
        }
    }
}
