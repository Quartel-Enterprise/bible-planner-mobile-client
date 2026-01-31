package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.bibleversion.domain.model.BibleVersion
import com.quare.bibleplanner.feature.bibleversion.domain.model.DownloadStatus
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
fun BibleVersionItem(
    version: BibleVersion,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = version.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = version.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when (version.status) {
                DownloadStatus.NotDownloaded -> {
                    IconButton(onClick = { onEvent(BibleVersionUiEvent.OnDownload(version.id)) }) {
                        Icon(Icons.Rounded.Download, contentDescription = "Download")
                    }
                }

                DownloadStatus.Downloaded -> {
                    IconButton(onClick = { onEvent(BibleVersionUiEvent.OnDelete(version.id)) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                }

                is DownloadStatus.Downloading -> {
                    IconButton(onClick = { onEvent(BibleVersionUiEvent.OnPause(version.id)) }) {
                        Icon(Icons.Rounded.Pause, contentDescription = "Pause")
                    }
                }

                is DownloadStatus.Paused -> {
                    IconButton(onClick = { onEvent(BibleVersionUiEvent.OnResume(version.id)) }) {
                        Icon(Icons.Rounded.Download, contentDescription = "Resume")
                    }
                }
            }
        }

        val progress = when (val status = version.status) {
            is DownloadStatus.Downloading -> status.progress
            is DownloadStatus.Paused -> status.progress
            else -> null
        }

        AnimatedVisibility(visible = progress != null) {
            if (progress != null) {
                VerticalSpacer(4)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
