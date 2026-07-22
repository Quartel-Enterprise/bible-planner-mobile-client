package com.quare.bibleplanner.feature.inappupdate.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.in_app_update.generated.resources.Res
import bibleplanner.feature.in_app_update.generated.resources.update_action_store
import bibleplanner.feature.in_app_update.generated.resources.update_available_title
import bibleplanner.feature.in_app_update.generated.resources.update_available_version
import bibleplanner.feature.in_app_update.generated.resources.update_description_store
import bibleplanner.feature.in_app_update.generated.resources.update_dismiss
import com.quare.bibleplanner.feature.inappupdate.presentation.component.UpdateIconBadge
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiEvent
import com.quare.bibleplanner.feature.inappupdate.presentation.model.InAppUpdateUiState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppUpdateContent(
    state: InAppUpdateUiState,
    onEvent: (InAppUpdateUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UpdateIconBadge(icon = Icons.Default.SystemUpdate)
        VerticalSpacer(12.dp)
        Text(
            text = stringResource(Res.string.update_available_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (state.versionName != null) {
            VerticalSpacer(4.dp)
            Text(
                text = stringResource(Res.string.update_available_version, state.versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        VerticalSpacer(10.dp)
        Text(
            text = stringResource(Res.string.update_description_store),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20.dp)
        Button(
            onClick = { onEvent(InAppUpdateUiEvent.OnUpdateClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(Res.string.update_action_store),
            )
        }
        VerticalSpacer(4.dp)
        TextButton(
            onClick = { onEvent(InAppUpdateUiEvent.OnDismiss) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_dismiss))
        }
    }
}
