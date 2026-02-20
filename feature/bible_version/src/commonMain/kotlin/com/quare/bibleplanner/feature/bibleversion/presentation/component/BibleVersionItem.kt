package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionItem(
    modifier: Modifier = Modifier,
    model: BibleSelectionModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    val versionId = model.version.id
    val onSelect = { onEvent(BibleVersionUiEvent.OnSelect(versionId)) }
    ListItem(
        modifier = modifier.clickable { onSelect() },
        leadingContent = {
            RadioButton(
                selected = model.isSelected,
                onClick = { onSelect() },
            )
        },
        headlineContent = {
            Text(
                text = model.version.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = versionId,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            BibleVersionItemDownloadStatusComponent(
                status = model.status,
                onEvent = { onEvent(it(versionId)) },
            )
        },
    )
}
