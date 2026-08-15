package com.quare.bibleplanner.feature.shareverse.presentation

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.shareverse.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ShareVerseViewModel(
    route: ShareVerseNavRoute,
    private val getVersesShareContent: GetVersesShareContent,
    trackEvent: TrackEvent,
) : TrackedViewModel<ShareVerseUiEvent>(trackEvent) {
    private val bookId = BookId.valueOf(route.bookId)
    private val chapterNumber = route.chapterNumber
    private val verseNumbers = route.verseNumbers

    private var shareContent: VersesShareContentModel? = null

    private val _uiState = MutableStateFlow(
        ShareVerseUiState(
            quote = "",
            reference = "",
            versionName = "",
            background = ShareCardBackground.VIOLET,
            font = ShareCardFont.LORA,
            isReady = false,
        ),
    )
    val uiState: StateFlow<ShareVerseUiState> = _uiState

    private val _uiAction = MutableSharedFlow<ShareVerseUiAction>()
    val uiAction: SharedFlow<ShareVerseUiAction> = _uiAction

    init {
        loadContent()
    }

    override fun handleEvent(event: ShareVerseUiEvent) {
        when (event) {
            ShareVerseUiEvent.OnShareAsTextClick -> shareAsText()

            ShareVerseUiEvent.OnShareAsImageClick -> openImageComposer()

            is ShareVerseUiEvent.OnBackgroundClick -> {
                _uiState.update { it.copy(background = event.background) }
                trackStyleChange()
            }

            is ShareVerseUiEvent.OnFontClick -> {
                _uiState.update { it.copy(font = event.font) }
                trackStyleChange()
            }

            is ShareVerseUiEvent.OnShareImageReady -> shareImage(event.imageBytes)

            ShareVerseUiEvent.OnDismiss ->
                viewModelScope.launch { _uiAction.emit(ShareVerseUiAction.NavigateBack) }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            val content = getVersesShareContent(
                bookId = bookId,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
            ) ?: return@launch
            shareContent = content
            _uiState.update {
                it.copy(
                    quote = content.text,
                    reference = content.reference,
                    versionName = content.versionName,
                    isReady = true,
                )
            }
        }
    }

    private fun shareAsText() {
        val content = shareContent ?: return
        trackShared(FORMAT_TEXT)
        viewModelScope.launch {
            _uiAction.emit(ShareVerseUiAction.ShareText(content))
        }
    }

    private fun openImageComposer() {
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_IMAGE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to verseNumbers.size),
        )
        viewModelScope.launch {
            _uiAction.emit(
                ShareVerseUiAction.NavigateToRoute(
                    ShareVerseImageNavRoute(
                        bookId = bookId.name,
                        chapterNumber = chapterNumber,
                        verseNumbers = verseNumbers,
                    ),
                ),
            )
        }
    }

    private fun shareImage(imageBytes: ByteArray) {
        val content = shareContent ?: return
        trackShared(FORMAT_IMAGE)
        viewModelScope.launch {
            _uiAction.emit(
                ShareVerseUiAction.ShareImage(
                    content = content,
                    imageBytes = imageBytes,
                ),
            )
        }
    }

    private fun trackShared(format: String) {
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARED,
            params = mapOf(
                AnalyticsParams.FORMAT to format,
                AnalyticsParams.VERSE_COUNT to verseNumbers.size,
            ),
        )
    }

    private fun trackStyleChange() {
        val state = uiState.value
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_STYLE_CHANGED,
            params = mapOf(
                AnalyticsParams.BACKGROUND to state.background.name.lowercase(),
                AnalyticsParams.FONT to state.font.name.lowercase(),
            ),
        )
    }

    private companion object {
        const val FORMAT_TEXT = "text"
        const val FORMAT_IMAGE = "image"
    }
}
