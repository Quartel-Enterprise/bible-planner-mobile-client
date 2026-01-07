package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.feature.more.presentation.factory.MoreUiStateFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MoreViewModel(
    uiStateFactory: MoreUiStateFactory,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val _uiState = MutableStateFlow<MoreUiState>(MoreUiState.Loading)
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        observe(uiStateFactory.create()) { state ->
            _uiState.update { state }
        }
    }

    fun onEvent(event: MoreUiEvent) {
        when (event) {
            is MoreUiEvent.OnItemClick -> {
                when (event.type) {
                    MoreOptionItemType.THEME -> {
                        emitAction(MoreUiAction.GoToTheme)
                    }

                    MoreOptionItemType.PRIVACY_POLICY -> {
                        emitAction(MoreUiAction.OpenLink(PRIVACY_URL))
                    }

                    MoreOptionItemType.TERMS -> {
                        emitAction(MoreUiAction.OpenLink(TERMS_URL))
                    }

                    MoreOptionItemType.BECOME_PRO -> {
                        emitAction(MoreUiAction.GoToPaywall)
                    }

                    MoreOptionItemType.INSTAGRAM -> {
                        emitAction(MoreUiAction.OpenLink(getInstagramUrl()))
                    }

                    MoreOptionItemType.EDIT_PLAN_START_DAY -> {
                        emitAction(MoreUiAction.GoToEditPlanStartDay)
                    }

                    MoreOptionItemType.DELETE_PROGRESS -> {
                        viewModelScope.launch {
                            val progress = calculateBibleProgress().first()
                            if (progress > 0) {
                                emitAction(MoreUiAction.GoToDeleteProgress)
                            } else {
                                emitAction(MoreUiAction.ShowNoProgressToDelete)
                            }
                        }
                    }

                    MoreOptionItemType.DONATE -> {
                        emitAction(MoreUiAction.OpenLink(SPONSOR_URL))
                    }
                }
            }

            MoreUiEvent.OnProCardClick -> {
                _uiState.update {
                    if (it is MoreUiState.Loaded) {
                        it.copy(showSubscriptionDetailsDialog = true)
                    } else {
                        it
                    }
                }
            }

            MoreUiEvent.OnDismissSubscriptionDetailsDialog -> {
                _uiState.update {
                    if (it is MoreUiState.Loaded) {
                        it.copy(showSubscriptionDetailsDialog = false)
                    } else {
                        it
                    }
                }
            }
        }
    }

    private fun getInstagramUrl(): String = when (Locale.current.language) {
        "pt" -> INSTAGRAM_PT_BR
        "es" -> INSTAGRAM_ES
        else -> INSTAGRAM_DEFAULT
    }

    private fun emitAction(action: MoreUiAction) {
        viewModelScope.launch {
            _uiAction.emit(action)
        }
    }

    companion object {
        private const val BASE_URL = "https://www.bibleplanner.app"
        private const val PRIVACY_URL = "$BASE_URL/privacy"
        private const val TERMS_URL = "$BASE_URL/terms"
        private const val SPONSOR_URL = "https://github.com/sponsors/Quartel-Enterprise"
        private const val INSTAGRAM_DEFAULT = "https://www.instagram.com/bible.planner"
        private const val INSTAGRAM_PT_BR = "$INSTAGRAM_DEFAULT.brasil"
        private const val INSTAGRAM_ES = "$INSTAGRAM_DEFAULT.espanol"
    }
}
