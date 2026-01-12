package com.quare.bibleplanner.feature.donation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.feature.donation.generated.DonationBuildKonfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DonationUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiAction = MutableSharedFlow<DonationUiAction>()
    val uiAction = _uiAction.asSharedFlow()

    fun onEvent(event: DonationUiEvent) {
        when (event) {
            DonationUiEvent.Dismiss -> {
                viewModelScope.launch {
                    _uiAction.emit(DonationUiAction.NavigateBack)
                }
            }

            is DonationUiEvent.Copy -> {
                viewModelScope.launch {
                    // Toggle: if already copied, clear it; otherwise, copy it
                    if (_uiState.value.copiedType == event.type) {
                        _uiState.update { it.copy(copiedType = null) }
                    } else {
                        val text = when (event.type) {
                            DonationType.BTC_ONCHAIN -> DonationBuildKonfig.BTC_ONCHAIN
                            DonationType.BTC_LIGHTNING -> DonationBuildKonfig.BTC_LIGHTNING
                            DonationType.USDT_ERC20 -> DonationBuildKonfig.USDT_ERC20
                            DonationType.USDT_TRC20 -> DonationBuildKonfig.USDT_TRC20
                            DonationType.PIX -> DonationBuildKonfig.PIX_KEY
                        }
                        _uiState.update { it.copy(copiedType = event.type) }
                        _uiAction.emit(DonationUiAction.Copy(text))
                    }
                }
            }

            DonationUiEvent.OpenGitHubSponsors -> {
                viewModelScope.launch {
                    _uiAction.emit(DonationUiAction.OpenUrl("https://github.com/sponsors/Quartel-Enterprise"))
                }
            }

            DonationUiEvent.ToggleBitcoin -> {
                _uiState.update { it.copy(isBitcoinExpanded = !it.isBitcoinExpanded) }
            }

            DonationUiEvent.ToggleUsdt -> {
                _uiState.update { it.copy(isUsdtExpanded = !it.isUsdtExpanded) }
            }

            DonationUiEvent.TogglePix -> {
                _uiState.update { it.copy(isPixExpanded = !it.isPixExpanded) }
            }

            DonationUiEvent.OpenPixQr -> {
                viewModelScope.launch {
                    _uiAction.emit(DonationUiAction.NavigateToPixQr)
                }
            }
        }
    }
}
