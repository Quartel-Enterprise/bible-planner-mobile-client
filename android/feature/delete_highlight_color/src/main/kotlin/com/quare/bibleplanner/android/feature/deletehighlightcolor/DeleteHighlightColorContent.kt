package com.quare.bibleplanner.android.feature.deletehighlightcolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.dialog.HideSceneDialogScrimEffect
import com.quare.bibleplanner.android.designsystem.dialog.rememberAlertDialogResolution
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.feature.read.presentation.deletecolor.DeleteHighlightColorUiEvent
import com.quare.bibleplanner.ui.component.highlight.toSwatchColor

private val colorDotSize = 34.dp

@Composable
internal fun DeleteHighlightColorContent(
    color: HighlightColor?,
    onEvent: (DeleteHighlightColorUiEvent) -> Unit,
) {
    val resolution = rememberAlertDialogResolution(color)
    val onCancel = { resolution.resolve { onEvent(DeleteHighlightColorUiEvent.OnCancelClick) } }
    HideSceneDialogScrimEffect()
    AlertDialog(
        onDismissRequest = onCancel,
        icon = color?.let { safeColor ->
            {
                Box(
                    modifier = Modifier
                        .size(colorDotSize)
                        .background(
                            color = safeColor.toSwatchColor(),
                            shape = CircleShape,
                        ),
                )
            }
        },
        title = { Text(text = stringResource(R.string.delete_highlight_color_title)) },
        text = { Text(text = stringResource(R.string.delete_highlight_color_message)) },
        /*
         * Why: three actions with long labels don't fit side by side, so Material stacks them end-aligned,
         * with the dismissive one last.
         */
        confirmButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = {
                        resolution.resolve {
                            onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = true))
                        }
                    },
                ) {
                    Text(text = stringResource(R.string.delete_highlight_color_keep_highlights))
                }
                TextButton(
                    onClick = {
                        resolution.resolve {
                            onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = false))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(text = stringResource(R.string.delete_highlight_color_and_highlights))
                }
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.delete_highlight_color_cancel))
                }
            }
        },
    )
}
