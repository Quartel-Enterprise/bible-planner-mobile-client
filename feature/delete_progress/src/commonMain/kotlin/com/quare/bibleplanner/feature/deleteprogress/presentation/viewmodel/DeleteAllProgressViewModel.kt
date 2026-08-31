package com.quare.bibleplanner.feature.deleteprogress.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.ResetAllProgressUseCase
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiEvent
import com.quare.bibleplanner.feature.deleteprogress.presentation.model.DeleteAllProgressUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class DeleteAllProgressViewModel(
    private val resetAllProgress: ResetAllProgressUseCase,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<DeleteAllProgressUiEvent>(trackEvent) {
    private val successFeedbackDuration: Duration = 700.milliseconds

    private val _uiState: MutableStateFlow<DeleteAllProgressUiState> =
        MutableStateFlow(DeleteAllProgressUiState.Idle)
    val uiState: StateFlow<DeleteAllProgressUiState> = _uiState.asStateFlow()

    override fun handleEvent(event: DeleteAllProgressUiEvent) {
        when (event) {
            DeleteAllProgressUiEvent.OnConfirmDelete -> {
                viewModelScope.launch {
                    _uiState.update { DeleteAllProgressUiState.Loading }
                    suspendRunCatching { resetAllProgress() }
                        .onSuccess {
                            trackEvent(
                                name = AnalyticsEventNames.PROGRESS_RESET_CONFIRMED,
                                params = emptyMap(),
                            )
                            _uiState.update { DeleteAllProgressUiState.Success }
                            delay(successFeedbackDuration)
                            navigator.navigateBack()
                        }.onFailure {
                            _uiState.update { DeleteAllProgressUiState.Idle }
                        }
                }
            }

            DeleteAllProgressUiEvent.OnCancel -> {
                viewModelScope.launch {
                    navigator.navigateBack()
                }
            }
        }
    }
}
