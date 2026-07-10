package com.quare.bibleplanner.feature.donation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.donation.generated.DonationBuildKonfig
import com.quare.bibleplanner.feature.donation.presentation.factory.DonationUiStateFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DonationViewModel(
    factory: DonationUiStateFactory,
    private val trackEvent: TrackEvent,
) : ViewModel() {
    private val _uiState = MutableStateFlow(factory.create())
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
                        trackEvent(
                            name = AnalyticsEventNames.DONATION_METHOD_COPIED,
                            params = mapOf(AnalyticsParams.METHOD to event.type.name.lowercase()),
                        )
                    }
                }
            }

            DonationUiEvent.OpenGitHubSponsors -> {
                trackEvent(
                    name = AnalyticsEventNames.GITHUB_SPONSORS_OPENED,
                    params = emptyMap(),
                )
                viewModelScope.launch {
                    _uiAction.emit(DonationUiAction.OpenUrl("https://github.com/sponsors/Quartel-Enterprise"))
                }
            }

            DonationUiEvent.ToggleBitcoin -> {
                val isExpanded = !_uiState.value.isBitcoinExpanded
                _uiState.update { it.copy(isBitcoinExpanded = isExpanded) }
                trackSectionToggled(
                    section = BITCOIN_SECTION,
                    isExpanded = isExpanded,
                )
            }

            DonationUiEvent.ToggleUsdt -> {
                val isExpanded = !_uiState.value.isUsdtExpanded
                _uiState.update { it.copy(isUsdtExpanded = isExpanded) }
                trackSectionToggled(
                    section = USDT_SECTION,
                    isExpanded = isExpanded,
                )
            }

            DonationUiEvent.TogglePix -> {
                val isExpanded = !_uiState.value.isPixExpanded
                _uiState.update { it.copy(isPixExpanded = isExpanded) }
                trackSectionToggled(
                    section = PIX_SECTION,
                    isExpanded = isExpanded,
                )
            }

            DonationUiEvent.OpenPixQr -> {
                viewModelScope.launch {
                    _uiAction.emit(DonationUiAction.NavigateToPixQr)
                }
            }
        }
    }

    private fun trackSectionToggled(
        section: String,
        isExpanded: Boolean,
    ) {
        trackEvent(
            name = AnalyticsEventNames.DONATION_SECTION_TOGGLED,
            params = mapOf(
                AnalyticsParams.SECTION to section,
                AnalyticsParams.IS_EXPANDED to isExpanded,
            ),
        )
    }

    private companion object {
        const val BITCOIN_SECTION = "bitcoin"
        const val USDT_SECTION = "usdt"
        const val PIX_SECTION = "pix"
    }
}
