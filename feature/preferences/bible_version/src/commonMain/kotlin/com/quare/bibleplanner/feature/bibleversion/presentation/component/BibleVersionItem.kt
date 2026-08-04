package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

private const val SELECTED_CONTAINER_ALPHA = 0.12f
private val radioButtonSize = 24.dp

@Composable
internal fun BibleVersionItem(
    modifier: Modifier = Modifier,
    model: BibleModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    val versionId = model.version.id
    val onSelect = { onEvent(BibleVersionUiEvent.OnSelect(versionId)) }
    val containerColor = if (model.isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CONTAINER_ALPHA)
    } else {
        Color.Transparent
    }
    ListItem(
        modifier = modifier.clickable { onSelect() },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = model.isSelected,
                    onClick = onSelect,
                    modifier = Modifier.size(radioButtonSize),
                )
                HorizontalSpacer(12)
                BibleVersionAbbreviationChip(
                    abbreviation = model.version.id.uppercase(),
                    isSelected = model.isSelected,
                )
            }
        },
        headlineContent = {
            Text(
                text = model.version.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (model.isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        supportingContent = {
            Column {
                BibleVersionItemSupportingContent(
                    downloadStatus = model.downloadStatus,
                    hasPendingUpdate = model.hasPendingUpdate,
                    size = model.version.size,
                )
                val downloadStatus = model.downloadStatus
                if (downloadStatus is DownloadStatusModel.InProgress) {
                    VerticalSpacer(8)
                    LinearProgressIndicator(
                        progress = { downloadStatus.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (downloadStatus is DownloadStatusModel.InProgress.Downloading) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
        trailingContent = {
            BibleVersionItemDownloadStatusComponent(
                status = model.downloadStatus,
                hasPendingUpdate = model.hasPendingUpdate,
                onEvent = { onEvent(it(versionId)) },
            )
        },
    )
}
