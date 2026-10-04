package com.quare.bibleplanner.tools.agentcli.fake

import androidx.lifecycle.ViewModel

internal abstract class EventViewModel<E : Any> : ViewModel() {
    val received = mutableListOf<E>()

    fun onEvent(event: E) {
        received += event
    }
}

internal class GenericEventViewModel : EventViewModel<SampleUiEvent>()
