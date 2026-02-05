package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersionModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

@Composable
internal fun BibleVersionsContent(
    modifier: Modifier = Modifier,
    versions: List<BibleVersionModel>,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
            text = "Bible Versions",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
            text = "Manage your downloaded Bible versions and select one to read.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyColumn {
            items(versions) { version ->
                BibleVersionItem(
                    version = version,
                    onEvent = onEvent,
                )
            }
        }
    }
}
