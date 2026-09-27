package com.quare.bibleplanner.feature.read.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import com.quare.bibleplanner.feature.read.presentation.model.VerseFocusUiModel
import com.quare.bibleplanner.feature.read.presentation.screen.findVerseItemIndex
import kotlinx.coroutines.delay

private const val FLASH_IN_MILLIS = 350
private const val FLASH_HOLD_MILLIS = 900L
private const val FLASH_OUT_MILLIS = 1_100
private val topMargin = 72.dp

@Composable
internal fun rememberVerseFlash(
    focus: VerseFocusUiModel?,
    chapters: List<ReadChapterUiModel>,
    listState: LazyListState,
    leadingItemCount: Int,
    onShown: () -> Unit,
): VerseFlash {
    val alpha = remember { Animatable(0f) }
    var flashingFocus by remember { mutableStateOf<VerseFocusUiModel?>(null) }
    val topMarginPx = with(LocalDensity.current) { topMargin.toPx() }
    val currentChapters by rememberUpdatedState(chapters)
    val currentLeadingItemCount by rememberUpdatedState(leadingItemCount)
    val currentOnShown by rememberUpdatedState(onShown)
    val hasChapters = chapters.isNotEmpty()
    LaunchedEffect(focus, hasChapters) {
        val safeFocus = focus?.takeIf { hasChapters } ?: return@LaunchedEffect
        val index = findVerseItemIndex(
            chapters = currentChapters,
            leadingItemCount = currentLeadingItemCount,
            focus = safeFocus,
        )
        if (index != null) {
            listState.scrollToItem(index)
            listState.scrollBy(-topMarginPx)
            flashingFocus = safeFocus
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(FLASH_IN_MILLIS),
            )
            delay(FLASH_HOLD_MILLIS)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(FLASH_OUT_MILLIS),
            )
            flashingFocus = null
        }
        currentOnShown()
    }
    return VerseFlash(
        focus = flashingFocus,
        alpha = { alpha.value },
    )
}
