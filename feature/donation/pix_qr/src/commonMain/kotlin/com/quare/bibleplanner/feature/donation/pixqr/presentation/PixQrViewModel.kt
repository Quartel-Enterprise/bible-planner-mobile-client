package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_share_message
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.getPlatform
import com.quare.bibleplanner.core.utils.locale.AppLanguage
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PixQrViewModel : ViewModel() {
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
                    val appStoreLink = getAppStoreLink()
                    val message = getString(Res.string.pix_qr_share_message, appStoreLink)

                    _uiAction.emit(PixQrUiAction.ShareQrCode(message))
                }
            }
        }
    }

    private fun getAppStoreLink(): String {
        val language = getCurrentLanguage()
        val platform = getPlatform()

        return when (platform) {
            Platform.IOS -> {
                val locale = when (language) {
                    AppLanguage.PORTUGUESE_BRAZIL -> "br"
                    AppLanguage.SPANISH -> "es"
                    else -> "us"
                }
                "https://apps.apple.com/$locale/app/bible-planner-reading-plans/id6756151777"
            }

            Platform.ANDROID -> {
                val locale = when (language) {
                    AppLanguage.PORTUGUESE_BRAZIL -> "pt-BR"
                    AppLanguage.SPANISH -> "es"
                    else -> "en"
                }
                "https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=$locale"
            }

            else -> {
                "https://bibleplanner.app"
            }
        }
    }
}
