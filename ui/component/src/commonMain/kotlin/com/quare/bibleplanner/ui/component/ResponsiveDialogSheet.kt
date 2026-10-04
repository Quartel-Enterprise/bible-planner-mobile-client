package com.quare.bibleplanner.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.close
import com.quare.bibleplanner.ui.component.dialog.createCardDialogProperties
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.LocalNavigationBarInsets
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard
import org.jetbrains.compose.resources.stringResource

private val wideLayoutMinWidth = 600.dp
private val centredTitleHorizontalPadding = 56.dp
private val defaultCardMaxWidth = 460.dp
private val cardVerticalMargin = 24.dp
private const val SHEET_SCRIM_ALPHA = 0.44f
private const val CARD_SCRIM_ALPHA = 0.52f
private const val CARD_HIDDEN_SCALE = 0.9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveDialogSheet(
    onCloseClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    isTitleCentred: Boolean = false,
    cardMaxWidth: Dp = defaultCardMaxWidth,
    sheetBottomBreathingRoom: Dp = 0.dp,
    sheetMaxHeightFraction: Float? = null,
    content: @Composable () -> Unit,
) {
    val sheetCloseGuard = rememberSheetCloseGuard()
    val isClosing = sheetCloseGuard.isClosing
    val onClose = onCloseClick?.let { close -> { sheetCloseGuard.close(close) } }
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val windowHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val contentMaxHeight = windowHeight - cardVerticalMargin * 2
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= wideLayoutMinWidth) {
            ResponsiveDialogSheetCard(
                onCloseClick = onClose,
                cardMaxWidth = cardMaxWidth,
                cardMaxHeight = contentMaxHeight,
            ) {
                CloseableContent(
                    onCloseClick = onClose,
                    title = title,
                    subtitle = subtitle,
                    isTitleCentred = isTitleCentred,
                    content = content,
                    contentMaxHeight = contentMaxHeight,
                    modifier = Modifier.blockPointerInput(isBlocked = isClosing),
                )
            }
        } else {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var dragHandleHeight by remember { mutableStateOf(0.dp) }
            SheetExitAnimationEffect(
                animateOut = sheetState::hide,
                animateBackIn = sheetState::show,
            )
            ModalBottomSheet(
                onDismissRequest = { onClose?.invoke() },
                sheetState = sheetState,
                sheetGesturesEnabled = !isClosing,
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = SHEET_SCRIM_ALPHA),
                dragHandle = {
                    BottomSheetDefaults.DragHandle(
                        modifier = if (sheetMaxHeightFraction == null) {
                            Modifier
                        } else {
                            Modifier.onSizeChanged { size ->
                                dragHandleHeight = with(density) { size.height.toDp() }
                            }
                        },
                    )
                },
            ) {
                val sheetMaxHeight = sheetMaxHeightFraction?.let { fraction ->
                    val sheetBottomInset = with(density) { WindowInsets.safeDrawing.getBottom(density).toDp() }
                    windowHeight * fraction - dragHandleHeight - sheetBottomInset
                }
                CloseableContent(
                    onCloseClick = onClose,
                    title = title,
                    subtitle = subtitle,
                    isTitleCentred = isTitleCentred,
                    content = content,
                    contentMaxHeight = contentMaxHeight,
                    modifier = Modifier
                        .sheetMaxHeight(sheetMaxHeight)
                        .windowInsetsPadding(LocalNavigationBarInsets.current)
                        .padding(bottom = sheetBottomBreathingRoom)
                        .blockPointerInput(isBlocked = isClosing),
                )
            }
        }
    }
}

/*
 * Why: capping the ModalBottomSheet modifier itself also shrinks the height M3 computes its
 * anchors from, which pins the sheet to the top of the screen. The cap goes on the content instead,
 * minus the drag handle and the bottom inset M3 pads the sheet with outside of it.
 */
private fun Modifier.sheetMaxHeight(maxHeight: Dp?): Modifier = if (maxHeight == null) {
    this
} else {
    heightIn(max = maxHeight)
}

@Composable
private fun CloseableContent(
    onCloseClick: (() -> Unit)?,
    title: String?,
    subtitle: String?,
    isTitleCentred: Boolean,
    content: @Composable () -> Unit,
    contentMaxHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .heightIn(max = contentMaxHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            if (title != null) {
                DialogHeader(
                    title = title,
                    subtitle = subtitle,
                    isCentred = isTitleCentred,
                )
            }
            content()
        }
        if (onCloseClick != null) {
            CommonIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.close),
                onClick = onCloseClick,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

/*
 * Why: a centred title shares its row with the close button, so it uses the smaller size and
 * is inset to clear it; left-aligned titles keep the display size.
 */
@Composable
private fun DialogHeader(
    title: String,
    subtitle: String?,
    isCentred: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isCentred) centredTitleHorizontalPadding else 16.dp,
                vertical = 16.dp,
            ),
        horizontalAlignment = if (isCentred) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            text = title,
            style = if (isCentred) {
                MaterialTheme.typography.titleSmall
            } else {
                MaterialTheme.typography.titleLarge
            },
            fontWeight = FontWeight.Bold,
            textAlign = if (isCentred) TextAlign.Center else TextAlign.Start,
        )
        if (subtitle != null) {
            VerticalSpacer(4.dp)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (isCentred) TextAlign.Center else TextAlign.Start,
            )
        }
    }
}

@Composable
private fun ResponsiveDialogSheetCard(
    onCloseClick: (() -> Unit)?,
    cardMaxWidth: Dp,
    cardMaxHeight: Dp,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { onCloseClick?.invoke() },
        properties = createCardDialogProperties(),
    ) {
        ClearDialogWindowDimEffect()
        val visibility = remember { Animatable(0f) }
        LaunchedEffect(visibility) { visibility.animateTo(1f) }
        SheetExitAnimationEffect(
            animateOut = { visibility.animateTo(0f) },
            animateBackIn = { visibility.animateTo(1f) },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = visibility.value }
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = CARD_SCRIM_ALPHA))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCloseClick?.invoke() },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = cardMaxWidth)
                        .fillMaxWidth()
                        .heightIn(max = cardMaxHeight)
                        .graphicsLayer {
                            val scale = lerp(
                                start = CARD_HIDDEN_SCALE,
                                stop = 1f,
                                fraction = visibility.value,
                            )
                            scaleX = scale
                            scaleY = scale
                        }.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    content()
                }
            }
        }
    }
}
