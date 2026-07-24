package com.quare.bibleplanner.feature.daystudy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyPanelRatio
import com.quare.bibleplanner.feature.daystudy.domain.usecase.ObserveDayStudyPanelReadingFractionUseCase
import com.quare.bibleplanner.feature.daystudy.domain.usecase.SetDayStudyPanelReadingFractionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DayStudyPanelViewModel(
    observeReadingFraction: ObserveDayStudyPanelReadingFractionUseCase,
    private val setReadingFraction: SetDayStudyPanelReadingFractionUseCase,
) : ViewModel() {
    val readingFraction: StateFlow<Float> = observeReadingFraction().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DayStudyPanelRatio.DEFAULT,
    )

    fun onReadingFractionChanged(fraction: Float) {
        viewModelScope.launch { setReadingFraction(fraction) }
    }
}
