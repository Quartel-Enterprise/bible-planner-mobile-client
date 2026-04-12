package com.quare.bibleplanner.feature.bibleversion.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import bibleplanner.feature.bible_version.generated.resources.Res
import bibleplanner.feature.bible_version.generated.resources.downloaded
import bibleplanner.feature.bible_version.generated.resources.downloading_progress
import bibleplanner.feature.bible_version.generated.resources.paused_progress
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BibleVersionItemSupportingContent(downloadStatus: DownloadStatusModel) {
    val text = when (downloadStatus) {
        DownloadStatusModel.Downloaded -> {
            stringResource(Res.string.downloaded)
        }

        is DownloadStatusModel.InProgress -> {
            when (downloadStatus) {
                is DownloadStatusModel.InProgress.Downloading -> stringResource(
                    Res.string.downloading_progress,
                    downloadStatus.progressStr,
                )

                is DownloadStatusModel.InProgress.Paused -> stringResource(Res.string.paused_progress, downloadStatus.progressStr)
            }
        }

        DownloadStatusModel.NotStarted -> {
            null
        }
    }
    text?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
