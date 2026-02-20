package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionItem(
    modifier: Modifier = Modifier,
    version: BibleVersionModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    val onSelect = { onEvent(BibleVersionUiEvent.OnSelect(version.id)) }
    ListItem(
        modifier = modifier.clickable { onSelect() },
        leadingContent = {
            RadioButton(
                selected = version.isSelected,
                onClick = { onSelect() },
            )
        },
        headlineContent = {
            Text(
                text = version.name,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = version.id,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            BibleVersionItemDownloadStatusComponent(
                status = version.status,
                onEvent = { onEvent(it(version.id)) },
            )
        },
    )
}
