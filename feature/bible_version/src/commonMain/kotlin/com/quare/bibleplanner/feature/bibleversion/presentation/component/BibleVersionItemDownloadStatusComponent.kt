package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.delete
import bibleplanner.feature.bible_version.generated.resources.download
import bibleplanner.feature.bible_version.generated.resources.pause
import bibleplanner.feature.bible_version.generated.resources.resume
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent
import com.quare.bibleplanner.ui.component.icon.CommonIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionItemDownloadStatusComponent(
    status: DownloadStatusModel,
    onEvent: ((versionId: String) -> BibleVersionUiEvent) -> Unit,
) {
    when (status) {
        DownloadStatusModel.NotStarted -> {
            CommonIconButton(
                imageVector = Icons.Rounded.Download,
                contentDescription = stringResource(Res.string.download),
                onClick = { onEvent(BibleVersionUiEvent::OnDownload) },
            )
        }

        DownloadStatusModel.Downloaded -> {
            DeleteIcon(onEvent)
        }

        is DownloadStatusModel.InProgress.Downloading -> {
            CircularProgressAction(
                progress = status.progress,
                imageVector = Icons.Rounded.Pause,
                contentDescription = stringResource(Res.string.pause),
                onClick = { onEvent(BibleVersionUiEvent::OnPause) },
            )
        }

        is DownloadStatusModel.InProgress.Paused -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressAction(
                    progress = status.progress,
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(Res.string.resume),
                    progressColor = MaterialTheme.colorScheme.outlineVariant,
                    onClick = { onEvent(BibleVersionUiEvent::OnResume) },
                )
                DeleteIcon(onEvent)
            }
        }
    }
}

@Composable
private fun DeleteIcon(onEvent: ((String) -> BibleVersionUiEvent) -> Unit) {
    CommonIconButton(
        imageVector = Icons.Rounded.Delete,
        contentDescription = stringResource(Res.string.delete),
        onClick = { onEvent(BibleVersionUiEvent::OnDelete) },
    )
}

@Composable
private fun CircularProgressAction(
    modifier: Modifier = Modifier,
    progress: Float,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    progressColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            strokeCap = StrokeCap.Round,
            color = progressColor,
        )
        CommonIconButton(
            modifier = Modifier.size(24.dp),
            imageVector = imageVector,
            contentDescription = contentDescription,
            onClick = onClick,
        )
    }
}
