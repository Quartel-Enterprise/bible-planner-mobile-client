package com.quare.bibleplanner.android.feature.bibleversion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.sheet.AppModalBottomSheet
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdateItem
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent

private val dividerStartInset = 56.dp

@Composable
internal fun PendingBibleUpdatesContent(
    pendingUpdates: List<PendingBibleUpdateItem>,
    onEvent: (PendingBibleUpdatesUiEvent) -> Unit,
) {
    val title = if (pendingUpdates.size > 1) {
        stringResource(R.string.bible_version_updates_available)
    } else {
        stringResource(R.string.bible_version_update_available)
    }
    AppModalBottomSheet(
        title = title,
        subtitle = stringResource(R.string.bible_version_updates_description),
        onDismiss = { onEvent(PendingBibleUpdatesUiEvent.OnDismissClick) },
    ) { sheetCloseGuard ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        ) {
            Column {
                pendingUpdates.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = dividerStartInset),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    PendingBibleUpdateRow(
                        item = item,
                        onToggle = { onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(item.id)) },
                    )
                }
            }
        }
        val selectedCount = pendingUpdates.count { it.isSelected }
        Button(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            enabled = selectedCount > 0,
            // Why: the ViewModel pops the sheet once the updates start; a second tap would pop the screen below.
            onClick = { sheetCloseGuard.close { onEvent(PendingBibleUpdatesUiEvent.OnUpdateClick) } },
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Rounded.Update,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing),
                text = if (selectedCount > 1) {
                    stringResource(R.string.bible_version_update_count, selectedCount)
                } else {
                    stringResource(R.string.bible_version_update)
                },
            )
        }
    }
}

@Composable
private fun PendingBibleUpdateRow(
    item: PendingBibleUpdateItem,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = item.isSelected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            ).padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = null,
        )
        BibleVersionAbbreviationChip(
            versionId = item.id,
            isSelected = false,
        )
        Column {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            item.size?.let { size ->
                Text(
                    text = megabytesText(size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
