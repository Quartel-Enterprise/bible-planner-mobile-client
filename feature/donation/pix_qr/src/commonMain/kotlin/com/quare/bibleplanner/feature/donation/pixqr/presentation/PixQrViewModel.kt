package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_share_message
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.domain.usecase.GetAppStoreLinkUseCase
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PixQrViewModel(
    private val getAppStoreLink: GetAppStoreLinkUseCase,
    private val navigator: Navigator,
    trackEvent: TrackEvent,
) : TrackedViewModel<PixQrUiEvent>(trackEvent) {
    val uiAction: SharedFlow<PixQrUiAction>
        field = MutableSharedFlow<PixQrUiAction>()

    override fun handleEvent(event: PixQrUiEvent) {
        when (event) {
            PixQrUiEvent.Dismiss -> navigator.navigateBack()

            PixQrUiEvent.Share -> {
                viewModelScope.launch {
                    val message = getString(Res.string.pix_qr_share_message, getAppStoreLink())

                    uiAction.emit(PixQrUiAction.ShareQrCode(message))
                }
            }
        }
    }
}
