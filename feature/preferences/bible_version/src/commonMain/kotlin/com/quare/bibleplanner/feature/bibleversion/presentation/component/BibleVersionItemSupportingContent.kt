package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import bibleplanner.feature.preferences.bible_version.generated.resources.Res
import bibleplanner.feature.preferences.bible_version.generated.resources.downloaded
import bibleplanner.feature.preferences.bible_version.generated.resources.downloading_progress
import bibleplanner.feature.preferences.bible_version.generated.resources.paused_progress
import bibleplanner.feature.preferences.bible_version.generated.resources.tap_to_download
import bibleplanner.feature.preferences.bible_version.generated.resources.update_available
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionItemSupportingContent(
    downloadStatus: DownloadStatusModel,
    hasPendingUpdate: Boolean,
) {
    if (downloadStatus == DownloadStatusModel.Downloaded && hasPendingUpdate) {
        Text(
            text = stringResource(Res.string.update_available),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        return
    }
    val text = when (downloadStatus) {
        DownloadStatusModel.Downloaded -> stringResource(Res.string.downloaded)

        is DownloadStatusModel.InProgress.Downloading -> stringResource(
            Res.string.downloading_progress,
            downloadStatus.progressStr,
        )

        is DownloadStatusModel.InProgress.Paused -> stringResource(
            Res.string.paused_progress,
            downloadStatus.progressStr,
        )

        DownloadStatusModel.NotStarted -> stringResource(Res.string.tap_to_download)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
