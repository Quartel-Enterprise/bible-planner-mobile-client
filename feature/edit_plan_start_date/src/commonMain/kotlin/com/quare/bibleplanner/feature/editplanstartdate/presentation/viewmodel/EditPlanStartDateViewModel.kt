package com.quare.bibleplanner.feature.editplanstartdate.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.GetFinalTimestampAfterEditionUseCase
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.date.toLocalDate
import com.quare.bibleplanner.core.date.toTimestampUTC
import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.usecase.SetPlanStartTimeUseCase
import com.quare.bibleplanner.feature.editplanstartdate.domain.usecase.ConvertUtcDateToLocalDateUseCase
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiEvent
import com.quare.bibleplanner.feature.editplanstartdate.presentation.model.EditPlanStartDateUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class EditPlanStartDateViewModel(
    private val planRepository: PlanRepository,
    private val setPlanStartTime: SetPlanStartTimeUseCase,
    private val getFinalTimestampAfterEdition: GetFinalTimestampAfterEditionUseCase,
    private val convertUtcDateToLocalDate: ConvertUtcDateToLocalDateUseCase,
    private val currentTimestampProvider: CurrentTimestampProvider,
    private val localDateTimeProvider: LocalDateTimeProvider,
) : ViewModel() {
    private val _uiState: MutableStateFlow<EditPlanStartDateUiState> =
        MutableStateFlow(EditPlanStartDateUiState.Loading)
    val uiState: StateFlow<EditPlanStartDateUiState> = _uiState.asStateFlow()
    private val _dismissUiAction: MutableSharedFlow<Unit> = MutableSharedFlow()
    val dismissUiAction: SharedFlow<Unit> = _dismissUiAction

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        observe(planRepository.getStartPlanTimestamp()) { startDate ->
            val initialTimestamp = startDate?.toTimestampUTC() ?: localDateTimeProvider
                .getLocalDateTime(
                    currentTimestampProvider.getCurrentTimestamp(),
                ).toLocalDate()
                .toTimestampUTC()
            _uiState.update {
                EditPlanStartDateUiState.Loaded(
                    initialTimestamp = initialTimestamp,
                )
            }
        }
    }

    fun onEvent(event: EditPlanStartDateUiEvent) {
        when (event) {
            is EditPlanStartDateUiEvent.OnDismissDialog -> {
                dismissDialog()
            }

            is EditPlanStartDateUiEvent.OnDateSelected -> {
                onDateSelected(event.utcDateMillis)
            }
        }
    }

    private fun dismissDialog() {
        viewModelScope.launch {
            _dismissUiAction.emit(Unit)
        }
    }

    private fun onDateSelected(utcDateMillis: Long) {
        viewModelScope.launch {
            setPlanStartTime(
                strategy = SetPlanStartTimeUseCase.Strategy.SpecificTime(
                    timestamp = getFinalTimestampAfterEdition(convertUtcDateToLocalDate(utcDateMillis)),
                ),
            )
            dismissDialog()
        }
    }
}
