package com.quare.bibleplanner.feature.day.presentation.component.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bibleplanner.feature.day.generated.resources.Res
import bibleplanner.feature.day.generated.resources.clear_notes
import bibleplanner.feature.day.generated.resources.notes
import com.quare.bibleplanner.ui.component.icon.DeleteIconButon
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NotesHeaderSection(
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.notes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        DeleteIconButon(onClick = onDeleteClick, contentDescription = stringResource(Res.string.clear_notes))
    }
}
