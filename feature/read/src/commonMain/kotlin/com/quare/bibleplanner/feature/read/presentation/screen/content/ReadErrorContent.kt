package com.quare.bibleplanner.feature.read.presentation.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.chapter_not_downloaded_error
import bibleplanner.feature.read.generated.resources.download_version
import bibleplanner.feature.read.generated.resources.download_version_with_size
import bibleplanner.feature.read.generated.resources.downloading_progress
import bibleplanner.feature.read.generated.resources.manage_bible_versions
import bibleplanner.feature.read.generated.resources.paused_progress
import bibleplanner.feature.read.generated.resources.resume_download
import bibleplanner.feature.read.generated.resources.retry
import bibleplanner.feature.read.generated.resources.size_progress
import bibleplanner.feature.read.generated.resources.status_with_size
import bibleplanner.feature.read.generated.resources.unknown_error_occurred
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.ui.component.spacer.HorizontalSpacer
import com.quare.bibleplanner.ui.component.text.megabytesText
import org.jetbrains.compose.resources.stringResource

private const val PROGRESS_BAR_WIDTH_FRACTION = 0.6f

@Composable
internal fun ReadErrorContent(
    modifier: Modifier = Modifier,
    state: ReadUiState.Error,
    onEvent: (ReadUiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        when (state) {
            is ReadUiState.Error.ChapterNotFound -> ChapterNotFoundContent(
                state = state,
                onEvent = onEvent,
            )

            is ReadUiState.Error.Unknown -> {
                ErrorMessage(text = stringResource(Res.string.unknown_error_occurred))
                Button(
                    onClick = { onEvent(state.errorUiEvent) },
                ) {
                    Text(text = stringResource(Res.string.retry))
                }
            }
        }
    }
}

@Composable
private fun ChapterNotFoundContent(
    state: ReadUiState.Error.ChapterNotFound,
    onEvent: (ReadUiEvent) -> Unit,
) {
    ErrorMessage(
        text = stringResource(
            resource = Res.string.chapter_not_downloaded_error,
            stringResource(state.bookStringResource),
            state.chapterNumber,
            state.selectedBibleVersionName,
        ),
    )
    when (val downloadStatus = state.downloadStatus) {
        is DownloadStatusModel.InProgress.Downloading -> DownloadProgress(
            inProgressStatus = downloadStatus,
            statusText = stringResource(
                Res.string.downloading_progress,
                downloadStatus.progressStr,
            ),
            versionSizeInBytes = state.versionSizeInBytes,
        )

        is DownloadStatusModel.InProgress.Paused -> {
            DownloadProgress(
                inProgressStatus = downloadStatus,
                statusText = stringResource(
                    Res.string.paused_progress,
                    downloadStatus.progressStr,
                ),
                versionSizeInBytes = state.versionSizeInBytes,
            )
            DownloadButton(
                text = stringResource(Res.string.resume_download),
                imageVector = Icons.Rounded.PlayArrow,
                onClick = { onEvent(ReadUiEvent.OnDownloadSelectedVersionClick) },
            )
        }

        DownloadStatusModel.NotStarted, DownloadStatusModel.Downloaded -> DownloadButton(
            text = state.versionSizeInBytes?.let { sizeInBytes ->
                stringResource(
                    Res.string.download_version_with_size,
                    megabytesText(sizeInBytes),
                )
            } ?: stringResource(Res.string.download_version),
            imageVector = Icons.Rounded.Download,
            onClick = { onEvent(ReadUiEvent.OnDownloadSelectedVersionClick) },
        )
    }
    TextButton(
        onClick = { onEvent(ReadUiEvent.ManageBibleVersions) },
    ) {
        Text(text = stringResource(Res.string.manage_bible_versions))
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun DownloadButton(
    text: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Button(onClick = onClick) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )
        HorizontalSpacer(8)
        Text(text = text)
    }
}

@Composable
private fun DownloadProgress(
    inProgressStatus: DownloadStatusModel.InProgress,
    statusText: String,
    versionSizeInBytes: Long?,
) {
    LinearProgressIndicator(
        progress = { inProgressStatus.progress },
        modifier = Modifier.fillMaxWidth(PROGRESS_BAR_WIDTH_FRACTION),
    )
    val text = if (versionSizeInBytes == null) {
        statusText
    } else {
        stringResource(
            Res.string.status_with_size,
            statusText,
            stringResource(
                Res.string.size_progress,
                megabytesText((versionSizeInBytes * inProgressStatus.progress).toLong()),
                megabytesText(versionSizeInBytes),
            ),
        )
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
