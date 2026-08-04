package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.update_all
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available_description
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available_title
import bibleplanner.feature.preferences.bible_version.generated.resources.update_later
import com.quare.bibleplanner.feature.bibleversion.presentation.model.PendingBibleUpdatesUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PendingBibleUpdatesContent(
    pendingVersionNames: List<String>,
    onEvent: (PendingBibleUpdatesUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.update_available_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        VerticalSpacer(8.dp)
        Text(
            text = stringResource(Res.string.update_available_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(12.dp)
        pendingVersionNames.forEach { versionName ->
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        VerticalSpacer(20.dp)
        Button(
            onClick = { onEvent(PendingBibleUpdatesUiEvent.OnUpdateClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_all))
        }
        VerticalSpacer(4.dp)
        TextButton(
            onClick = { onEvent(PendingBibleUpdatesUiEvent.OnDismissClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_later))
        }
    }
}
