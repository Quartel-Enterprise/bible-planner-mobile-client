package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionItem(
    modifier: Modifier = Modifier,
    model: BibleModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    val versionId = model.version.id
    val onSelect = { onEvent(BibleVersionUiEvent.OnSelect(versionId)) }
    ListItem(
        modifier = modifier
            .clip(
                shape = MaterialTheme.shapes.medium,
            ).then(
                if (model.isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium,
                    )
                } else {
                    Modifier
                },
            ).clickable { onSelect() },
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
        supportingContent = { BibleVersionItemSupportingContent(model.downloadStatus) },
        trailingContent = {
            BibleVersionItemDownloadStatusComponent(
                status = model.downloadStatus,
                onEvent = { onEvent(it(versionId)) },
            )
        },
    )
}
