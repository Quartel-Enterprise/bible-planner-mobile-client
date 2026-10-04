package com.quare.bibleplanner.tools.agentcli.fake

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class AppLevelViewModel : ViewModel() {
    val theme: StateFlow<String> = MutableStateFlow("SYSTEM")

    var lastEvent: SampleUiEvent? = null
        private set

    fun onEvent(event: SampleUiEvent) {
        lastEvent = event
    }
}
