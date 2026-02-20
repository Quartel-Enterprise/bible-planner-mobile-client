package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.books.domain.model.BibleVersionModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionItem(
    version: BibleVersionModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    ListItem(
        leadingContent = {
            RadioButton(
                selected = version.isSelected,
                onClick = {},
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
