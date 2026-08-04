package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.update
import bibleplanner.feature.preferences.bible_version.generated.resources.update_count
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdateItem
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PendingBibleUpdatesContent(
    pendingUpdates: List<PendingBibleUpdateItem>,
    onEvent: (PendingBibleUpdatesUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        pendingUpdates.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            PendingBibleUpdateRow(
                item = item,
                onToggle = { onEvent(PendingBibleUpdatesUiEvent.OnToggleVersion(item.id)) },
            )
        }
        VerticalSpacer(20.dp)
        val selectedCount = pendingUpdates.count { it.isSelected }
        Button(
            onClick = { onEvent(PendingBibleUpdatesUiEvent.OnUpdateClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedCount > 0,
        ) {
            Icon(
                imageVector = Icons.Rounded.Update,
                contentDescription = null,
            )
            HorizontalSpacer(8)
            val label = if (selectedCount > 1) {
                stringResource(Res.string.update_count, selectedCount)
            } else {
                stringResource(Res.string.update)
            }
            Text(text = label)
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
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggle() },
        )
        BibleVersionAbbreviationChip(
            abbreviation = item.id.uppercase(),
            isSelected = false,
        )
        HorizontalSpacer(4)
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
