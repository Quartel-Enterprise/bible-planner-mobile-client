package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.delete
import bibleplanner.feature.preferences.bible_version.generated.resources.download
import bibleplanner.feature.preferences.bible_version.generated.resources.pause
import bibleplanner.feature.preferences.bible_version.generated.resources.resume
import bibleplanner.feature.preferences.bible_version.generated.resources.update
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionItemDownloadStatusComponent(
    status: DownloadStatusModel,
    hasPendingUpdate: Boolean,
    onEvent: ((versionId: String) -> BibleVersionUiEvent) -> Unit,
) {
    when (status) {
        DownloadStatusModel.NotStarted -> {
            CommonIconButton(
                imageVector = Icons.Rounded.Download,
                contentDescription = stringResource(Res.string.download),
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onEvent(BibleVersionUiEvent::OnDownload) },
            )
        }

        DownloadStatusModel.Downloaded -> if (hasPendingUpdate) {
            CommonIconButton(
                imageVector = Icons.Rounded.Update,
                contentDescription = stringResource(Res.string.update),
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onEvent(BibleVersionUiEvent::OnUpdate) },
            )
        } else {
            DeleteBibleVersionButton(onEvent = onEvent)
        }

        is DownloadStatusModel.InProgress -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DeleteBibleVersionButton(onEvent = onEvent)
                if (status is DownloadStatusModel.InProgress.Downloading) {
                    CommonIconButton(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = stringResource(Res.string.pause),
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { onEvent(BibleVersionUiEvent::OnPause) },
                    )
                } else {
                    CommonIconButton(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(Res.string.resume),
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { onEvent(BibleVersionUiEvent::OnResume) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteBibleVersionButton(onEvent: ((versionId: String) -> BibleVersionUiEvent) -> Unit) {
    CommonIconButton(
        imageVector = Icons.Rounded.Delete,
        contentDescription = stringResource(Res.string.delete),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onEvent(BibleVersionUiEvent::OnDelete) },
    )
}
