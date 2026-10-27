package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_photo_load_failed
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.books.domain.usecase.GetVersesShareContent
import com.quare.bibleplanner.core.image.DecodeImageBitmap
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetReadingStreak
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveIsProUser
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareProOption
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiState
import com.quare.bibleplanner.ui.theme.font.ShareCardFont
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ShareVerseViewModel(
    private val getVersesShareContent: GetVersesShareContent,
    private val getReadingStreak: GetReadingStreak,
    private val decodeImageBitmap: DecodeImageBitmap,
    val platform: Platform,
    private val navigator: Navigator,
    route: ShareVerseNavRoute,
    observeIsProUser: ObserveIsProUser,
    trackEvent: TrackEvent,
) : TrackedViewModel<ShareVerseUiEvent>(trackEvent) {
    private val bookId = BookId.valueOf(route.bookId)
    private val chapterNumber = route.chapterNumber
    private val verseNumbers = route.verseNumbers

    private var shareContent: VersesShareContentModel? = null

    val uiState: StateFlow<ShareVerseUiState>
        field = MutableStateFlow(
            ShareVerseUiState(
                quote = "",
                reference = "",
                versionAbbreviation = "",
                background = ShareCardBackgroundUiModel.Preset(ShareCardBackground.VIOLET),
                customColor = ShareCardCustomColor(
                    hue = DEFAULT_CUSTOM_HUE,
                    lightness = DEFAULT_CUSTOM_LIGHTNESS,
                ),
                isColorPickerOpen = false,
                font = ShareCardFont.LORA,
                isWatermarkShown = true,
                streakDays = 0,
                isStreakShown = false,
                isProUser = false,
                isReady = false,
            ),
        )

    val uiAction: SharedFlow<ShareVerseUiAction>
        field = MutableSharedFlow<ShareVerseUiAction>()

    init {
        loadContent()
        loadStreak()
        observe(observeIsProUser()) { isPro ->
            uiState.update { it.copy(isProUser = isPro) }
        }
    }

    override fun handleEvent(event: ShareVerseUiEvent) {
        when (event) {
            ShareVerseUiEvent.OnShareAsTextClick -> shareAsText()

            ShareVerseUiEvent.OnShareAsImageClick -> openImageComposer()

            is ShareVerseUiEvent.OnBackgroundClick -> changeBackground(
                ShareCardBackgroundUiModel.Preset(event.background),
            )

            is ShareVerseUiEvent.OnFontClick -> {
                uiState.update { it.copy(font = event.font) }
                trackStyleChange()
            }

            ShareVerseUiEvent.OnCustomColorClick -> toggleCustomColor()

            is ShareVerseUiEvent.OnCustomColorChange -> changeCustomColor(
                ShareCardCustomColor(
                    hue = event.hue,
                    lightness = event.lightness,
                ),
            )

            ShareVerseUiEvent.OnPhotoBackgroundClick -> openPhotoPicker()

            is ShareVerseUiEvent.OnPhotoPicked -> event.file?.let(::applyPhoto)

            ShareVerseUiEvent.OnStreakToggleClick -> toggleStreak()

            ShareVerseUiEvent.OnWatermarkToggleClick -> toggleWatermark()

            is ShareVerseUiEvent.OnShareImageReady -> shareImage(event.imageBytes)

            is ShareVerseUiEvent.OnDownloadImageReady -> saveImage(event.imageBytes)

            ShareVerseUiEvent.OnDismiss -> navigator.navigateBack()
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
            uiState.update {
                it.copy(
                    quote = content.text,
                    reference = content.reference,
                    versionAbbreviation = content.versionAbbreviation,
                    isReady = true,
                )
            }
        }
    }

    // Why: the streak is a garnish on the card, so a failure just leaves its toggle hidden.
    private fun loadStreak() {
        viewModelScope.launch {
            suspendRunCatching { getReadingStreak() }.onSuccess { streakDays ->
                uiState.update { it.copy(streakDays = streakDays) }
            }
        }
    }

    private fun shareAsText() {
        val content = shareContent ?: return
        trackShared(FORMAT_TEXT)
        viewModelScope.launch {
            uiAction.emit(ShareVerseUiAction.ShareText(content))
        }
    }

    private fun openImageComposer() {
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_IMAGE_OPENED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to verseNumbers.size),
        )
        navigator.navigate(
            ShareVerseImageNavRoute(
                bookId = bookId.name,
                chapterNumber = chapterNumber,
                verseNumbers = verseNumbers,
            ),
        )
    }

    private fun changeBackground(background: ShareCardBackgroundUiModel) {
        uiState.update {
            it.copy(
                background = background,
                isColorPickerOpen = false,
            )
        }
        trackStyleChange()
    }

    private fun toggleCustomColor() {
        val state = uiState.value
        if (!state.isProUser) {
            openProPaywall(ShareProOption.CUSTOM_COLOR)
            return
        }
        val isOpen = !state.isColorPickerOpen
        uiState.update {
            it.copy(
                background = ShareCardBackgroundUiModel.Custom(it.customColor),
                isColorPickerOpen = isOpen,
            )
        }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_COLOR_PICKER_TOGGLED,
            params = mapOf(AnalyticsParams.IS_OPEN to isOpen),
        )
        if (state.background !is ShareCardBackgroundUiModel.Custom) {
            trackStyleChange()
        }
    }

    // Why: Pro can expire while the picker is open, so a stale slider drag must not restyle the card.
    private fun changeCustomColor(color: ShareCardCustomColor) {
        if (!uiState.value.isProUser) return
        uiState.update {
            it.copy(
                customColor = color,
                background = ShareCardBackgroundUiModel.Custom(color),
            )
        }
    }

    private fun openPhotoPicker() {
        if (!uiState.value.isProUser) {
            openProPaywall(ShareProOption.PHOTO)
            return
        }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_PHOTO_PICKER_OPENED,
            params = emptyMap(),
        )
        viewModelScope.launch {
            uiAction.emit(ShareVerseUiAction.LaunchPhotoPicker)
        }
    }

    private fun applyPhoto(file: PlatformFile) {
        if (!uiState.value.isProUser) return
        viewModelScope.launch {
            decodeImageBitmap(file)
                .onSuccess { image -> changeBackground(ShareCardBackgroundUiModel.Photo(image)) }
                .onFailure { uiAction.emit(ShareVerseUiAction.ShowMessage(Res.string.share_photo_load_failed)) }
        }
    }

    private fun toggleStreak() {
        val isShown = !uiState.value.isStreakShown
        uiState.update { it.copy(isStreakShown = isShown) }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_STREAK_TOGGLED,
            params = mapOf(
                AnalyticsParams.IS_ENABLED to isShown,
                AnalyticsParams.STREAK_DAYS to uiState.value.streakDays,
            ),
        )
    }

    private fun toggleWatermark() {
        val state = uiState.value
        if (!state.isProUser) {
            openProPaywall(ShareProOption.WATERMARK)
            return
        }
        val isShown = !state.isWatermarkShown
        uiState.update { it.copy(isWatermarkShown = isShown) }
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_WATERMARK_TOGGLED,
            params = mapOf(AnalyticsParams.IS_ENABLED to isShown),
        )
    }

    private fun openProPaywall(option: ShareProOption) {
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_PRO_OPTION_LOCKED_CLICKED,
            params = mapOf(AnalyticsParams.OPTION to option.key),
        )
        navigator.navigate(PaywallTeaserNavRoute(PaywallTeaserReason.SHARE_IMAGE_STYLE))
    }

    private fun shareImage(imageBytes: ByteArray) {
        val content = shareContent ?: return
        trackShared(FORMAT_IMAGE)
        viewModelScope.launch {
            uiAction.emit(
                ShareVerseUiAction.ShareImage(
                    content = content,
                    imageBytes = imageBytes,
                ),
            )
        }
    }

    private fun saveImage(imageBytes: ByteArray) {
        trackEvent(
            name = AnalyticsEventNames.VERSE_SHARE_IMAGE_DOWNLOADED,
            params = mapOf(AnalyticsParams.VERSE_COUNT to verseNumbers.size),
        )
        val fileName = listOf(
            FILE_NAME_PREFIX,
            bookId.name.lowercase(),
            chapterNumber,
            listOf(verseNumbers.min(), verseNumbers.max()).distinct().joinToString(separator = "-"),
        ).joinToString(separator = "_")
        viewModelScope.launch {
            uiAction.emit(
                ShareVerseUiAction.SaveImage(
                    imageBytes = imageBytes,
                    fileName = "$fileName.png",
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
                AnalyticsParams.BACKGROUND to state.background.analyticsKey,
                AnalyticsParams.FONT to state.font.name.lowercase(),
            ),
        )
    }

    private companion object {
        const val FORMAT_TEXT = "text"
        const val FORMAT_IMAGE = "image"
        const val FILE_NAME_PREFIX = "bible_planner"
        const val DEFAULT_CUSTOM_HUE = 265
        const val DEFAULT_CUSTOM_LIGHTNESS = 42
    }
}
