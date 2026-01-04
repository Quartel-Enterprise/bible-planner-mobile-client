package com.quare.bibleplanner.feature.more.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_premium
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase
import com.quare.bibleplanner.feature.more.presentation.factory.MoreMenuOptionsFactory
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiAction
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MoreViewModel(
    private val isFreeUser: IsFreeUserUseCase,
) : ViewModel() {

    private val _uiAction = MutableSharedFlow<MoreUiAction>()
    val uiAction: SharedFlow<MoreUiAction> = _uiAction
    private val baseItems = MoreMenuOptionsFactory.options
    private val _uiState = MutableStateFlow(
        MoreUiState(items = baseItems),
    )
    val uiState: StateFlow<MoreUiState> = _uiState

    init {
        viewModelScope.launch {
            if (isFreeUser()) {
                val premiumItem = MoreMenuItemPresentationModel(
                    name = Res.string.become_premium,
                    icon = Icons.Default.Star,
                    event = MoreUiEvent.ON_BECOME_PREMIUM_CLICK,
                )
                _uiState.update {
                    it.copy(items = listOf(premiumItem) + baseItems)
                }
            }
        }
    }

    fun onEvent(event: MoreUiEvent) {
        emitAction(
            when (event) {
                MoreUiEvent.ON_THEME_CLICK -> MoreUiAction.GoToTheme
                MoreUiEvent.ON_PRIVACY_CLICK -> MoreUiAction.OpenLink(PRIVACY_URL)
                MoreUiEvent.ON_TERMS_CLICK -> MoreUiAction.OpenLink(TERMS_URL)
                MoreUiEvent.ON_BECOME_PREMIUM_CLICK -> MoreUiAction.GoToPaywall
            }
        )
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
    }
}
