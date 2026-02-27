package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.english
import bibleplanner.feature.bible_version.generated.resources.portuguese_brazil
import bibleplanner.feature.bible_version.generated.resources.spanish
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.bibleVersionsListComponent(
    selectionMap: Map<Language, List<BibleModel>>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    selectionMap.keys.forEach { language ->
        item {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(
                    when (language) {
                        Language.ENGLISH -> Res.string.english
                        Language.PORTUGUESE_BRAZIL -> Res.string.portuguese_brazil
                        Language.SPANISH -> Res.string.spanish
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        selectionMap[language]?.let { versions ->
            items(versions) { version ->
                BibleVersionItem(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    model = version,
                    onEvent = onEvent,
                )
            }
        }
    }
}
