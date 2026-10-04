package com.quare.bibleplanner.tools.agentcli.fake

import androidx.lifecycle.ViewModel
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.DayNavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DayScreenViewModel(
    private val navigator: Navigator,
    route: DayNavRoute,
) : ViewModel() {
    val uiState: StateFlow<String> = MutableStateFlow("day ${route.dayNumber}")

    val count: StateFlow<Int>
        field = MutableStateFlow(0)

    val isWide: StateFlow<Boolean?>
        field = MutableStateFlow<Boolean?>(null)

    var isCleared: Boolean = false
        private set

    fun onEvent(event: DayScreenUiEvent) {
        when (event) {
            DayScreenUiEvent.OnBackClick -> navigator.navigateBack()
            DayScreenUiEvent.OnCount -> count.value++
            is DayScreenUiEvent.OnWidthClassChanged -> isWide.value = event.isWide
        }
    }

    override fun onCleared() {
        isCleared = true
    }
}
