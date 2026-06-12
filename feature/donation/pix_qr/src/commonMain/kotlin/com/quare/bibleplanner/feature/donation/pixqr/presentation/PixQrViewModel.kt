package com.quare.bibleplanner.feature.donation.pixqr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bibleplanner.feature.donation.pix_qr.generated.resources.Res
import bibleplanner.feature.donation.pix_qr.generated.resources.pix_qr_share_message
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.locale.Language
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class PixQrViewModel(
    private val languageProvider: LanguageProvider,
    private val platform: Platform,
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
                    val appStoreLink = getAppStoreLink()
                    val message = getString(Res.string.pix_qr_share_message, appStoreLink)

                    _uiAction.emit(PixQrUiAction.ShareQrCode(message))
                }
            }
        }
    }

    private fun getAppStoreLink(): String {
        val language = languageProvider.getAppLanguage()

        return when (platform) {
            Platform.Ios -> {
                val locale = when (language) {
                    Language.PORTUGUESE_BRAZIL -> "br"
                    Language.SPANISH -> "es"
                    else -> "us"
                }
                "https://apps.apple.com/$locale/app/bible-planner-reading-plans/id6756151777"
            }

            Platform.Android -> {
                val locale = when (language) {
                    Language.PORTUGUESE_BRAZIL -> "pt-BR"
                    Language.SPANISH -> "es"
                    else -> "en"
                }
                "https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=$locale"
            }

            is Platform.Desktop -> {
                "https://bibleplanner.app"
            }
        }
    }
}
