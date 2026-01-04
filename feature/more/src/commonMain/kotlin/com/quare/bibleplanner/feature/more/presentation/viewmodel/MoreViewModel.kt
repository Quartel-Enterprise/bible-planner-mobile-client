package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.usecase.CalculateBibleProgressUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsInstagramLinkVisibleUseCase
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MoreViewModel(
    private val isFreeUser: IsFreeUserUseCase,
    private val isInstagramLinkVisible: IsInstagramLinkVisibleUseCase,
    private val calculateBibleProgress: CalculateBibleProgressUseCase,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val _uiState = MutableStateFlow(
        MoreUiState(items = MoreMenuOptionsFactory.baseOptions),
    )
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        viewModelScope.launch {
            val isFreeDeferred = async { isFreeUser() }
            val isInstagramDeferred = async { isInstagramLinkVisible() }
            if (isFreeDeferred.await()) {
                _uiState.update {
                    it.copy(items = listOf(MoreMenuOptionsFactory.premiumItemOption) + it.items)
                }
            }
            if (isInstagramDeferred.await()) {
                _uiState.update { it.copy(items = it.items + listOf(MoreMenuOptionsFactory.instagramOption)) }
            }
        }
    }

    fun onEvent(event: MoreUiEvent) {
        when (event) {
            MoreUiEvent.ON_THEME_CLICK -> {
                emitAction(MoreUiAction.GoToTheme)
            }

            MoreUiEvent.ON_PRIVACY_CLICK -> {
                emitAction(MoreUiAction.OpenLink(PRIVACY_URL))
            }

            MoreUiEvent.ON_TERMS_CLICK -> {
                emitAction(MoreUiAction.OpenLink(TERMS_URL))
            }

            MoreUiEvent.ON_BECOME_PREMIUM_CLICK -> {
                emitAction(MoreUiAction.GoToPaywall)
            }

            MoreUiEvent.ON_INSTAGRAM_CLICK -> {
                emitAction(MoreUiAction.OpenLink(getInstagramUrl()))
            }

            MoreUiEvent.ON_EDIT_PLAN_START_DAY_CLICK -> {
                emitAction(MoreUiAction.GoToEditPlanStartDay)
            }

            MoreUiEvent.ON_DELETE_PROGRESS_CLICK -> {
                viewModelScope.launch {
                    val progress = calculateBibleProgress().first()
                    if (progress > 0) {
                        emitAction(MoreUiAction.GoToDeleteProgress)
                    } else {
                        emitAction(MoreUiAction.ShowNoProgressToDelete)
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

        private const val INSTAGRAM_PT_BR = "https://www.instagram.com/bible.planner.brasil"
        private const val INSTAGRAM_ES = "https://www.instagram.com/bible.planner.espanol"
        private const val INSTAGRAM_DEFAULT = "https://www.instagram.com/bible.planner"
    }
}
