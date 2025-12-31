package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.notes_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotesPlaceholder(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(Res.string.notes_placeholder),
        style = MaterialTheme.typography.bodyMedium,
    )
}
