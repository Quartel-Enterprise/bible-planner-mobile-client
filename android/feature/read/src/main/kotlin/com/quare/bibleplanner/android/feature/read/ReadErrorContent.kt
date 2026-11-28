package com.quare.bibleplanner.android.feature.read

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.util.getBookName
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadContentUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadHeaderUiModel
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

private const val PROGRESS_BAR_WIDTH_FRACTION = 0.6f
private const val BYTES_IN_MEGABYTE = 1_000_000
private const val DECIMAL_PLACE = 10

@Composable
internal fun ReadErrorContent(
    header: ReadHeaderUiModel,
    content: ReadContentUiState.Error,
    onEvent: (ReadUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        when (content) {
            is ReadContentUiState.Error.ChapterNotFound -> ChapterNotFoundContent(
                header = header,
                content = content,
                onEvent = onEvent,
            )

            is ReadContentUiState.Error.Unknown -> {
                ErrorMessage(text = stringResource(R.string.read_unknown_error_occurred))
                Button(
                    onClick = { onEvent(content.errorUiEvent) },
                ) {
                    Text(text = stringResource(R.string.read_retry))
                }
            }
        }
    }
}

@Composable
private fun ChapterNotFoundContent(
    header: ReadHeaderUiModel,
    content: ReadContentUiState.Error.ChapterNotFound,
    onEvent: (ReadUiEvent) -> Unit,
) {
    ErrorMessage(
        text = stringResource(
            R.string.read_chapter_not_downloaded_error,
            header.bookId.getBookName(),
            header.chapterNumber,
            content.selectedBibleVersionName,
        ),
    )
    when (val downloadStatus = content.downloadStatus) {
        is DownloadStatusModel.InProgress.Downloading -> DownloadProgressIndicator(
            inProgressStatus = downloadStatus,
            statusText = stringResource(
                R.string.read_downloading_progress,
                downloadStatus.progressStr,
            ),
            versionSizeInBytes = content.versionSizeInBytes,
        )

        is DownloadStatusModel.InProgress.Paused -> {
            DownloadProgressIndicator(
                inProgressStatus = downloadStatus,
                statusText = stringResource(
                    R.string.read_paused_progress,
                    downloadStatus.progressStr,
                ),
                versionSizeInBytes = content.versionSizeInBytes,
            )
            DownloadButton(
                text = stringResource(R.string.read_resume_download),
                imageVector = Icons.Rounded.PlayArrow,
                onClick = { onEvent(ReadUiEvent.OnDownloadSelectedVersionClick) },
            )
        }

        DownloadStatusModel.NotStarted, DownloadStatusModel.Downloaded -> DownloadButton(
            text = content.versionSizeInBytes?.let { sizeInBytes ->
                stringResource(
                    R.string.read_download_version_with_size,
                    megabytesText(sizeInBytes),
                )
            } ?: stringResource(R.string.read_download_version),
            imageVector = Icons.Rounded.Download,
            onClick = { onEvent(ReadUiEvent.OnDownloadSelectedVersionClick) },
        )
    }
    TextButton(
        onClick = { onEvent(ReadUiEvent.ManageBibleVersions) },
    ) {
        Text(text = stringResource(R.string.read_manage_bible_versions))
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
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

@Composable
private fun DownloadProgressIndicator(
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
            R.string.read_status_with_size,
            statusText,
            stringResource(
                R.string.read_size_progress,
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

@Composable
private fun megabytesText(bytes: Long): String = stringResource(
    R.string.read_megabytes_size,
    bytes / BYTES_IN_MEGABYTE,
    bytes % BYTES_IN_MEGABYTE * DECIMAL_PLACE / BYTES_IN_MEGABYTE,
)
