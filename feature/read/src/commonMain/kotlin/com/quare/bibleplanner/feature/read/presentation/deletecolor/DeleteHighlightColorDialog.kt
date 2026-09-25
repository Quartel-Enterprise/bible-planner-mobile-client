package com.quare.bibleplanner.feature.read.presentation.deletecolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.cancel
import bibleplanner.feature.read.generated.resources.delete_color_and_highlights
import bibleplanner.feature.read.generated.resources.delete_color_keep_highlights
import bibleplanner.feature.read.generated.resources.delete_highlight_color_message
import bibleplanner.feature.read.generated.resources.delete_highlight_color_title
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialog
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogAction
import com.quare.bibleplanner.ui.component.dialog.AppAlertDialogActionStyle
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private val colorDotSize = 34.dp

@Composable
internal fun DeleteHighlightColorDialog(
    color: HighlightColor?,
    onEvent: (DeleteHighlightColorUiEvent) -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.delete_highlight_color_title),
        text = stringResource(Res.string.delete_highlight_color_message),
        actions = listOf(
            AppAlertDialogAction(
                text = stringResource(Res.string.delete_color_keep_highlights),
                style = AppAlertDialogActionStyle.DEFAULT,
                isEnabled = true,
                onClick = { onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = true)) },
            ),
            AppAlertDialogAction(
                text = stringResource(Res.string.delete_color_and_highlights),
                style = AppAlertDialogActionStyle.DESTRUCTIVE,
                isEnabled = true,
                onClick = { onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = false)) },
            ),
            AppAlertDialogAction(
                text = stringResource(Res.string.cancel),
                style = AppAlertDialogActionStyle.CANCEL,
                isEnabled = true,
                onClick = { onEvent(DeleteHighlightColorUiEvent.OnCancelClick) },
            ),
        ),
        onDismissRequest = { onEvent(DeleteHighlightColorUiEvent.OnCancelClick) },
        icon = color?.let { safeColor ->
            {
                Box(
                    modifier = Modifier
                        .size(colorDotSize)
                        .clip(CircleShape)
                        .background(safeColor.toSwatchColor()),
                )
            }
        },
    )
}
