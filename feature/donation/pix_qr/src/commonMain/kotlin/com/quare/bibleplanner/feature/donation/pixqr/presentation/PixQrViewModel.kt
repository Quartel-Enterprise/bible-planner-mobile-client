package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_share_message
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PixQrViewModel(
    private val getAppStoreLink: GetAppStoreLinkUseCase,
) : ViewModel() {
    private val _uiAction = MutableSharedFlow<PixQrUiAction>()
    val uiAction = _uiAction.asSharedFlow()

    fun onEvent(event: PixQrUiEvent) {
        when (event) {
            PixQrUiEvent.Dismiss -> {
                viewModelScope.launch {
                    _uiAction.emit(PixQrUiAction.NavigateBack)
                }
            }

            PixQrUiEvent.Share -> {
                viewModelScope.launch {
                    val message = getString(Res.string.pix_qr_share_message, getAppStoreLink())

                    _uiAction.emit(PixQrUiAction.ShareQrCode(message))
                }
            }
        }
    }
}
