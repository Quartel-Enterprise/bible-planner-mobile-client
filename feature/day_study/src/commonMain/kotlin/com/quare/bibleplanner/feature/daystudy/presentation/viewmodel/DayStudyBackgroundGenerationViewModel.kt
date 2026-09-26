package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.daystudy.domain.coordinator.DayStudyGenerationCoordinator
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationJob
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationStatus
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiEvent
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyBackgroundGenerationUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

internal class DayStudyBackgroundGenerationViewModel(
    private val generationCoordinator: DayStudyGenerationCoordinator,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<DayStudyBackgroundGenerationUiEvent>(trackEvent) {
    val uiState: StateFlow<DayStudyBackgroundGenerationUiState>
        field = MutableStateFlow<DayStudyBackgroundGenerationUiState>(
            DayStudyBackgroundGenerationUiState(
                isVisible = false,
                jobs = emptyList(),
            ),
        )

    init {
        observeVisibleJobs()
    }

    override fun handleEvent(event: DayStudyBackgroundGenerationUiEvent) {
        when (event) {
            is DayStudyBackgroundGenerationUiEvent.OnOpenClick -> onOpenClick(event.job)
            is DayStudyBackgroundGenerationUiEvent.OnDismissClick -> onDismissClick(event.keys)
        }
    }

    private fun observeVisibleJobs() {
        combine(
            generationCoordinator.jobs,
            generationCoordinator.activeKey,
            generationCoordinator.dismissedKeys,
        ) { jobs, activeKey, dismissedKeys ->
            jobs.filter { job ->
                job.key != activeKey &&
                    job.key !in dismissedKeys &&
                    job.status !is DayStudyGenerationStatus.Failed
            }
        }.onEach(::onVisibleJobsChanged)
            .launchIn(viewModelScope)
    }

    private fun onVisibleJobsChanged(visibleJobs: List<DayStudyGenerationJob>) {
        uiState.update { state ->
            state.copy(
                isVisible = visibleJobs.isNotEmpty(),
                jobs = visibleJobs.ifEmpty { state.jobs },
            )
        }
    }

    private fun onOpenClick(job: DayStudyGenerationJob) {
        generationCoordinator.requestOpen(job.key)
        navigator.navigate(job.dayRoute)
    }

    private fun onDismissClick(keys: List<String>) {
        keys.forEach(generationCoordinator::dismissFromCard)
    }
}
