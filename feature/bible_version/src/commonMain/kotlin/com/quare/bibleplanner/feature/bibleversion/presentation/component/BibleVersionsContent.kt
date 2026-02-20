package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.bible_versions
import bibleplanner.feature.bible_version.generated.resources.manage_bible_versions_description
import com.quare.bibleplanner.core.books.domain.model.BibleSelectionModel
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionsContent(
    modifier: Modifier = Modifier,
    selectionMap: Map<Language, List<BibleSelectionModel>>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(
                vertical = 8.dp,
            ),
            text = stringResource(Res.string.bible_versions),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(Res.string.manage_bible_versions_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalSpacer(8)
        BibleVersionsListComponent(
            selectionMap = selectionMap,
            onEvent = onEvent,
        )
    }
}
