package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.downloaded
import bibleplanner.feature.preferences.bible_version.generated.resources.downloading_progress
import bibleplanner.feature.preferences.bible_version.generated.resources.paused_progress
import bibleplanner.feature.preferences.bible_version.generated.resources.size_progress
import bibleplanner.feature.preferences.bible_version.generated.resources.status_with_size
import bibleplanner.feature.preferences.bible_version.generated.resources.tap_to_download
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.ui.component.text.megabytesText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionItemSupportingContent(
    downloadStatus: DownloadStatusModel,
    hasPendingUpdate: Boolean,
    size: Long?,
) {
    if (downloadStatus == DownloadStatusModel.Downloaded && hasPendingUpdate) {
        Text(
            text = withSize(
                status = stringResource(Res.string.update_available),
                size = size,
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        return
    }
    val text = when (downloadStatus) {
        DownloadStatusModel.Downloaded -> withSize(
            status = stringResource(Res.string.downloaded),
            size = size,
        )

        is DownloadStatusModel.InProgress.Downloading -> withDownloadedSize(
            status = stringResource(Res.string.downloading_progress, downloadStatus.progressStr),
            progress = downloadStatus.progress,
            size = size,
        )

        is DownloadStatusModel.InProgress.Paused -> withDownloadedSize(
            status = stringResource(Res.string.paused_progress, downloadStatus.progressStr),
            progress = downloadStatus.progress,
            size = size,
        )

        DownloadStatusModel.NotStarted -> if (size == null) {
            stringResource(Res.string.tap_to_download)
        } else {
            megabytesText(size)
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun withSize(
    status: String,
    size: Long?,
): String = if (size == null) {
    status
} else {
    stringResource(
        Res.string.status_with_size,
        status,
        megabytesText(size),
    )
}

@Composable
private fun withDownloadedSize(
    status: String,
    progress: Float,
    size: Long?,
): String = if (size == null) {
    status
} else {
    stringResource(
        Res.string.status_with_size,
        status,
        stringResource(
            Res.string.size_progress,
            megabytesText((size * progress).toLong()),
            megabytesText(size),
        ),
    )
}
