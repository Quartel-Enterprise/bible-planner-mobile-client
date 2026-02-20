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
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.english
import bibleplanner.feature.bible_version.generated.resources.portuguese_brazil
import bibleplanner.feature.bible_version.generated.resources.spanish
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionsListComponent(
    modifier: Modifier = Modifier,
    selectionMap: Map<Language, List<BibleModel>>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        selectionMap.keys.forEach { language ->
            item {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = when (language) {
                        Language.ENGLISH -> stringResource(Res.string.english)
                        Language.PORTUGUESE_BRAZIL -> stringResource(Res.string.portuguese_brazil)
                        Language.SPANISH -> stringResource(Res.string.spanish)
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
