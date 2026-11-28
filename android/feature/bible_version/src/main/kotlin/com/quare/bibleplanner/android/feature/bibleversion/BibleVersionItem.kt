package com.quare.bibleplanner.android.feature.bibleversion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.feature.bibleversion.presentation.model.BibleVersionUiEvent

private const val SELECTED_CONTAINER_ALPHA = 0.12f

@Composable
internal fun BibleVersionItem(
    model: BibleModel,
    onEvent: (BibleVersionUiEvent) -> Unit,
) {
    val versionId = model.version.id
    ListItem(
        modifier = Modifier.selectable(
            selected = model.isSelected,
            role = Role.RadioButton,
            onClick = { onEvent(BibleVersionUiEvent.OnSelect(versionId)) },
        ),
        colors = ListItemDefaults.colors(
            containerColor = if (model.isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_CONTAINER_ALPHA)
            } else {
                Color.Transparent
            },
        ),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = model.isSelected,
                    onClick = null,
                )
                BibleVersionAbbreviationChip(
                    modifier = Modifier.padding(start = 12.dp),
                    versionId = versionId,
                    isSelected = model.isSelected,
                )
            }
        },
        headlineContent = {
            Text(
                text = model.version.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (model.isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        supportingContent = {
            Column {
                BibleVersionStatusText(
                    downloadStatus = model.downloadStatus,
                    hasPendingUpdate = model.hasPendingUpdate,
                    size = model.version.size,
                )
                val downloadStatus = model.downloadStatus
                if (downloadStatus is DownloadStatusModel.InProgress) {
                    LinearProgressIndicator(
                        progress = { downloadStatus.progress },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        color = if (downloadStatus is DownloadStatusModel.InProgress.Downloading) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        },
        trailingContent = {
            BibleVersionActionsComponent(
                status = model.downloadStatus,
                hasPendingUpdate = model.hasPendingUpdate,
                onAction = { createEvent -> onEvent(createEvent(versionId)) },
            )
        },
    )
}

@Composable
private fun BibleVersionActionsComponent(
    status: DownloadStatusModel,
    hasPendingUpdate: Boolean,
    onAction: ((versionId: String) -> BibleVersionUiEvent) -> Unit,
) {
    when (status) {
        DownloadStatusModel.NotStarted -> ActionButton(
            imageVector = Icons.Rounded.Download,
            contentDescription = stringResource(R.string.bible_version_download),
            tint = MaterialTheme.colorScheme.primary,
            onClick = { onAction(BibleVersionUiEvent::OnDownload) },
        )

        DownloadStatusModel.Downloaded -> if (hasPendingUpdate) {
            ActionButton(
                imageVector = Icons.Rounded.Update,
                contentDescription = stringResource(R.string.bible_version_update),
                tint = MaterialTheme.colorScheme.primary,
                onClick = { onAction(BibleVersionUiEvent::OnUpdate) },
            )
        } else {
            DeleteButton(onAction = onAction)
        }

        is DownloadStatusModel.InProgress -> Row(verticalAlignment = Alignment.CenterVertically) {
            DeleteButton(onAction = onAction)
            if (status is DownloadStatusModel.InProgress.Downloading) {
                ActionButton(
                    imageVector = Icons.Rounded.Pause,
                    contentDescription = stringResource(R.string.bible_version_pause),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onAction(BibleVersionUiEvent::OnPause) },
                )
            } else {
                ActionButton(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.bible_version_resume),
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onAction(BibleVersionUiEvent::OnResume) },
                )
            }
        }
    }
}

@Composable
private fun DeleteButton(onAction: ((versionId: String) -> BibleVersionUiEvent) -> Unit) {
    ActionButton(
        imageVector = Icons.Rounded.Delete,
        contentDescription = stringResource(R.string.bible_version_delete),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = { onAction(BibleVersionUiEvent::OnDelete) },
    )
}

@Composable
private fun ActionButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = LocalContentColor.current,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Composable
private fun BibleVersionStatusText(
    downloadStatus: DownloadStatusModel,
    hasPendingUpdate: Boolean,
    size: Long?,
) {
    if (downloadStatus == DownloadStatusModel.Downloaded && hasPendingUpdate) {
        Text(
            text = withSize(
                status = stringResource(R.string.bible_version_update_available),
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
            status = stringResource(R.string.bible_version_downloaded),
            size = size,
        )

        is DownloadStatusModel.InProgress.Downloading -> withDownloadedSize(
            status = stringResource(
                R.string.bible_version_downloading_progress,
                downloadStatus.progressStr,
            ),
            progress = downloadStatus.progress,
            size = size,
        )

        is DownloadStatusModel.InProgress.Paused -> withDownloadedSize(
            status = stringResource(
                R.string.bible_version_paused_progress,
                downloadStatus.progressStr,
            ),
            progress = downloadStatus.progress,
            size = size,
        )

        DownloadStatusModel.NotStarted -> if (size == null) {
            stringResource(R.string.bible_version_tap_to_download)
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
        R.string.bible_version_status_with_size,
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
        R.string.bible_version_status_with_size,
        status,
        stringResource(
            R.string.bible_version_size_progress,
            megabytesText((size * progress).toLong()),
            megabytesText(size),
        ),
    )
}
