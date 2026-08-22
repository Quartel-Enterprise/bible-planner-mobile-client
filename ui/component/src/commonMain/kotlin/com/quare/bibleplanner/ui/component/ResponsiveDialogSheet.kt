package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.close
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.utils.LocalNavigationBarInsets
import org.jetbrains.compose.resources.stringResource

private val wideLayoutMinWidth = 600.dp
private val centredTitleHorizontalPadding = 56.dp
private val defaultCardMaxWidth = 460.dp
private val cardVerticalMargin = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveDialogSheet(
    onCloseClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    isTitleCentred: Boolean = false,
    cardMaxWidth: Dp = defaultCardMaxWidth,
    content: @Composable () -> Unit,
) {
    DialogWindowDimEffect()
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= wideLayoutMinWidth) {
            ResponsiveDialogSheetCard(
                onCloseClick = onCloseClick,
                cardMaxWidth = cardMaxWidth,
                cardMaxHeight = maxHeight - cardVerticalMargin * 2,
            ) {
                CloseableContent(
                    onCloseClick = onCloseClick,
                    title = title,
                    subtitle = subtitle,
                    isTitleCentred = isTitleCentred,
                    content = content,
                )
            }
        } else {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { onCloseClick?.invoke() },
                sheetState = sheetState,
            ) {
                CloseableContent(
                    onCloseClick = onCloseClick,
                    title = title,
                    subtitle = subtitle,
                    isTitleCentred = isTitleCentred,
                    content = content,
                    modifier = Modifier.windowInsetsPadding(LocalNavigationBarInsets.current),
                )
            }
        }
    }
}

@Composable
private fun CloseableContent(
    onCloseClick: (() -> Unit)?,
    title: String?,
    subtitle: String?,
    isTitleCentred: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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

/**
 * A centred title shares its row with the close button, so it is set at the smaller size that fits
 * beside it and is inset to stay clear of it. Left-aligned titles keep the display size they had.
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCloseClick?.invoke() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = cardMaxWidth)
                .fillMaxWidth()
                .heightIn(max = cardMaxHeight)
                .clickable(
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
