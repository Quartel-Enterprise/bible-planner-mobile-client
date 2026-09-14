package com.quare.bibleplanner.feature.bibleversion.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ShouldShowBibleUpdatePromptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class PendingBibleUpdatesPromptViewModel(
    shouldShowBibleUpdatePrompt: ShouldShowBibleUpdatePromptUseCase,
) : ViewModel() {
    val shouldPrompt: StateFlow<Boolean>
        field = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            shouldPrompt.value = shouldShowBibleUpdatePrompt()
        }
    }

    fun onPromptConsumed() {
        shouldPrompt.value = false
    }
}
