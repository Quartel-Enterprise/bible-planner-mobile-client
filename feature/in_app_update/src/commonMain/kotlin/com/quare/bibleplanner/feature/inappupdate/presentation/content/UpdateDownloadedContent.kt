package com.quare.bibleplanner.feature.inappupdate.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadDone
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
import bibleplanner.feature.in_app_update.generated.resources.Res
import bibleplanner.feature.in_app_update.generated.resources.update_downloaded_message
import bibleplanner.feature.in_app_update.generated.resources.update_downloaded_title
import bibleplanner.feature.in_app_update.generated.resources.update_restart_later
import bibleplanner.feature.in_app_update.generated.resources.update_restart_now
import com.quare.bibleplanner.feature.inappupdate.presentation.component.UpdateIconBadge
import com.quare.bibleplanner.feature.inappupdate.presentation.model.UpdateDownloadedUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UpdateDownloadedContent(onEvent: (UpdateDownloadedUiEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UpdateIconBadge(icon = Icons.Default.DownloadDone)
        VerticalSpacer(12.dp)
        Text(
            text = stringResource(Res.string.update_downloaded_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        VerticalSpacer(8.dp)
        Text(
            text = stringResource(Res.string.update_downloaded_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20.dp)
        Button(
            onClick = { onEvent(UpdateDownloadedUiEvent.OnRestartNowClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_restart_now))
        }
        VerticalSpacer(4.dp)
        TextButton(
            onClick = { onEvent(UpdateDownloadedUiEvent.OnLaterClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.update_restart_later))
        }
    }
}
