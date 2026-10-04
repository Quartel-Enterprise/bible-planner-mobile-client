package com.quare.bibleplanner.tools.agentcli.fake

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

internal class SampleViewModel : ViewModel() {
    val uiState: StateFlow<SampleContent>
        field = MutableStateFlow<SampleContent>(SampleContent.Loading)

    val uiAction: SharedFlow<String>
        field = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val greeting: Flow<String> = flowOf("hello")

    val messages: Flow<String> = flowOf("toast")

    val selection: StateFlow<String?> = MutableStateFlow(null)

    fun onEvent(event: SampleUiEvent) {
        when (event) {
            is SampleUiEvent.OnCount -> uiState.value = SampleContent.Loaded(
                items = List(event.amount) { index -> index },
                label = null,
            )

            SampleUiEvent.OnReset -> uiAction.tryEmit("reset")

            else -> {}
        }
    }

    fun computeDouble(value: Int): Int = value * 2

    fun reload() {}

    suspend fun loadGreeting(name: String): String {
        delay(1)
        return "hi $name"
    }

    fun findOverloaded(value: Int): Int = value

    fun findOverloaded(value: String): String = value
}
