package com.quare.bibleplanner.feature.read.presentation.appearance

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.read.domain.model.ReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.model.ReaderFontSize
import com.quare.bibleplanner.feature.read.domain.model.ReaderSettingsModel
import com.quare.bibleplanner.feature.read.domain.usecase.ObserveReaderSettings
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFocusAid
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFont
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderFontSize
import com.quare.bibleplanner.feature.read.domain.usecase.SetReaderVerticalReading
import com.quare.bibleplanner.ui.theme.font.ReaderFont
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReaderAppearanceViewModel(
    private val observeReaderSettings: ObserveReaderSettings,
    private val setReaderFontSize: SetReaderFontSize,
    private val setReaderFont: SetReaderFont,
    private val setReaderFocusAid: SetReaderFocusAid,
    private val setReaderVerticalReading: SetReaderVerticalReading,
    trackEvent: TrackEvent,
) : TrackedViewModel<ReaderAppearanceUiEvent>(trackEvent) {
    private val isFontMenuExpanded = MutableStateFlow(false)

    private val _uiAction = MutableSharedFlow<ReaderAppearanceUiAction>()
    val uiAction: SharedFlow<ReaderAppearanceUiAction> = _uiAction

    val uiState: StateFlow<ReaderAppearanceUiState> = combine(
        observeReaderSettings(),
        isFontMenuExpanded,
    ) { settings, isExpanded ->
        ReaderAppearanceUiState(
            settings = settings,
            isFontMenuExpanded = isExpanded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReaderAppearanceUiState(
            settings = ReaderSettingsModel(
                fontSizeSp = ReaderFontSize.DEFAULT,
                font = ReaderFont.LORA,
                isRulerEnabled = false,
                isFocusedVerseEnabled = false,
                isVerticalReadingEnabled = false,
            ),
            isFontMenuExpanded = false,
        ),
    )

    override fun handleEvent(event: ReaderAppearanceUiEvent) {
        when (event) {
            is ReaderAppearanceUiEvent.OnFontSizeChange ->
                viewModelScope.launch { setReaderFontSize(event.fontSizeSp) }

            is ReaderAppearanceUiEvent.OnFontSizeChangeFinished -> {
                trackEvent(
                    name = AnalyticsEventNames.READER_FONT_SIZE_CHANGED,
                    params = mapOf(AnalyticsParams.FONT_SIZE to event.fontSizeSp),
                )
            }

            is ReaderAppearanceUiEvent.OnFontClick -> {
                trackEvent(
                    name = AnalyticsEventNames.READER_FONT_CHANGED,
                    params = mapOf(AnalyticsParams.FONT to event.font.name.lowercase()),
                )
                viewModelScope.launch { setReaderFont(event.font) }
            }

            is ReaderAppearanceUiEvent.OnFontMenuToggle -> {
                isFontMenuExpanded.update { event.isExpanded }
                trackEvent(
                    name = AnalyticsEventNames.READER_FONT_MENU_TOGGLED,
                    params = mapOf(AnalyticsParams.IS_EXPANDED to event.isExpanded),
                )
            }

            is ReaderAppearanceUiEvent.OnFocusAidChange -> {
                trackEvent(
                    name = AnalyticsEventNames.READER_FOCUS_AID_CHANGED,
                    params = mapOf(
                        AnalyticsParams.FOCUS_AID to event.focusAid.name.lowercase(),
                        AnalyticsParams.SOURCE to SOURCE_APPEARANCE,
                    ),
                )
                viewModelScope.launch { setReaderFocusAid(event.focusAid) }
            }

            is ReaderAppearanceUiEvent.OnVerticalReadingChange -> {
                trackEvent(
                    name = AnalyticsEventNames.READER_VERTICAL_READING_TOGGLED,
                    params = mapOf(AnalyticsParams.IS_ENABLED to event.isEnabled),
                )
                viewModelScope.launch { setReaderVerticalReading(event.isEnabled) }
            }

            ReaderAppearanceUiEvent.OnDismiss ->
                viewModelScope.launch { _uiAction.emit(ReaderAppearanceUiAction.NavigateBack) }
        }
    }

    private companion object {
        const val SOURCE_APPEARANCE = "appearance_sheet"
    }
}
