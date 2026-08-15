package com.quare.bibleplanner.feature.read.presentation.deletecolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.quare.bibleplanner.feature.read.presentation.utils.toSwatchColor
import org.jetbrains.compose.resources.stringResource

private val colorDotSize = 34.dp

@Composable
internal fun DeleteHighlightColorDialog(
    color: HighlightColor?,
    onEvent: (DeleteHighlightColorUiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(DeleteHighlightColorUiEvent.OnCancelClick) },
        icon = color?.let { safeColor ->
            {
                Row(
                    modifier = Modifier
                        .size(colorDotSize)
                        .clip(CircleShape)
                        .background(safeColor.toSwatchColor()),
                    verticalAlignment = Alignment.CenterVertically,
                ) {}
            }
        },
        title = { Text(text = stringResource(Res.string.delete_highlight_color_title)) },
        text = { Text(text = stringResource(Res.string.delete_highlight_color_message)) },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = true))
                    },
                ) {
                    Text(text = stringResource(Res.string.delete_color_keep_highlights))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onEvent(DeleteHighlightColorUiEvent.OnConfirmClick(shouldKeepHighlights = false))
                    },
                ) {
                    Text(text = stringResource(Res.string.delete_color_and_highlights))
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onEvent(DeleteHighlightColorUiEvent.OnCancelClick) },
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
            }
        },
    )
}
