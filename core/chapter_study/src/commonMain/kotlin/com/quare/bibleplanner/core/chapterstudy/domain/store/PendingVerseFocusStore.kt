package com.quare.bibleplanner.core.chapterstudy.domain.store

import com.quare.bibleplanner.core.chapterstudy.domain.model.PendingVerseFocusModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PendingVerseFocusStore {
    val pending: StateFlow<PendingVerseFocusModel?>
        field = MutableStateFlow<PendingVerseFocusModel?>(null)

    fun request(focus: PendingVerseFocusModel) {
        pending.value = focus
    }

    fun consume(focus: PendingVerseFocusModel) {
        pending.update { current -> current.takeIf { it != focus } }
    }
}
