package com.quare.bibleplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.close
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

/**
 * Presents [content] as a bottom sheet on narrow layouts (phones) or as a centered dialog card on
 * wide layouts (tablet/desktop), with a close button pinned to the top end in both cases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResponsiveDialogSheet(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= WideLayoutMinWidth) {
            ResponsiveDialogSheetCard(onCloseClick = onCloseClick) {
                CloseableContent(
                    onCloseClick = onCloseClick,
                    content = content,
                )
            }
        } else {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)
            ModalBottomSheet(onDismissRequest = onCloseClick, sheetState = sheetState) {
                CloseableContent(
                    onCloseClick = onCloseClick,
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun CloseableContent(
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box {
        content()
        CommonIconButton(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.close),
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun ResponsiveDialogSheetCard(
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.42f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCloseClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 460.dp)
                .fillMaxWidth()
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

private val WideLayoutMinWidth = 600.dp
