package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.mark_as_read
import bibleplanner.feature.read.generated.resources.mark_as_unread
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangeReadStatusButton(
    modifier: Modifier = Modifier,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    if (isRead) {
        OutlinedButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            Text(stringResource(Res.string.mark_as_unread))
        }
    } else {
        Button(
            modifier = modifier,
            onClick = onClick,
        ) {
            Text(stringResource(Res.string.mark_as_read))
        }
    }
}
