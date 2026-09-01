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
    private val _shouldPrompt = MutableStateFlow(false)
    val shouldPrompt: StateFlow<Boolean> = _shouldPrompt

    init {
        viewModelScope.launch {
            _shouldPrompt.value = shouldShowBibleUpdatePrompt()
        }
    }

    fun onPromptConsumed() {
        _shouldPrompt.value = false
    }
}
