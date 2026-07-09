package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
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
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer

private const val SELECTED_CONTAINER_ALPHA = 0.12f

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
                )
                HorizontalSpacer(8)
                BibleVersionAbbreviationChip(
                    abbreviation = model.version.name.toAbbreviation(),
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
        supportingContent = { BibleVersionItemSupportingContent(model.downloadStatus) },
        trailingContent = {
            BibleVersionItemDownloadStatusComponent(
                status = model.downloadStatus,
                onEvent = { onEvent(it(versionId)) },
            )
        },
    )
}

private fun String.toAbbreviation(): String = split(" ")
    .filter { it.isNotEmpty() }
    .joinToString("") { it.first().uppercase() }
    .take(4)
