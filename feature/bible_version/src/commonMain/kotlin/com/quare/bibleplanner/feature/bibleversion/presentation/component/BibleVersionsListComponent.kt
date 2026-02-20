package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionsListComponent(
    modifier: Modifier = Modifier,
    selectionMap: Map<Language, List<BibleSelectionModel>>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        selectionMap.keys.forEach { language ->
            item {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = when (language) {
                        Language.ENGLISH -> "English"
                        Language.PORTUGUESE_BRAZIL -> "Portuguese (Brazil)"
                        Language.SPANISH -> "Spanish"
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            selectionMap[language]?.let { versions ->
                items(versions) { version ->
                    BibleVersionItem(
                        modifier = Modifier.padding(vertical = 8.dp).clip(MaterialTheme.shapes.medium),
                        model = version,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}
