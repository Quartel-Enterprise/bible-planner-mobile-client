package com.quare.bibleplanner.feature.studyunlock.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bibleplanner.feature.study_unlock.generated.resources.Res
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_body
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_not_now
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_remaining_today
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_subscribe
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_title
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_video_loading
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_video_unavailable
import bibleplanner.feature.study_unlock.generated.resources.study_unlock_watch_video
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiEvent
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiState
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockVideoState
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val iconBoxSize = 56.dp
private val iconBoxCornerRadius = 16.dp
private val iconSize = 28.dp
private val bodyMaxWidth = 300.dp
private val progressSize = 18.dp
private val progressStrokeWidth = 2.dp

@Composable
internal fun StudyUnlockSheet(
    uiState: StudyUnlockUiState,
    onEvent: (StudyUnlockUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlaying = uiState.videoState == StudyUnlockVideoState.PLAYING
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(iconBoxSize)
                .clip(RoundedCornerShape(iconBoxCornerRadius))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                imageVector = Icons.Rounded.LockOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        VerticalSpacer(12)
        Text(
            text = stringResource(Res.string.study_unlock_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(6)
        Text(
            modifier = Modifier.widthIn(max = bodyMaxWidth),
            text = stringResource(Res.string.study_unlock_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        VerticalSpacer(20)
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPlaying,
            onClick = { onEvent(StudyUnlockUiEvent.OnSubscribeClick) },
        ) {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(text = stringResource(Res.string.study_unlock_subscribe))
        }
        VerticalSpacer(8)
        WatchVideoButton(
            videoState = uiState.videoState,
            onClick = { onEvent(StudyUnlockUiEvent.OnWatchVideoClick) },
        )
        VerticalSpacer(6)
        Text(
            text = videoCaption(uiState),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(
            enabled = !isPlaying,
            onClick = { onEvent(StudyUnlockUiEvent.OnDismiss) },
        ) {
            Text(text = stringResource(Res.string.study_unlock_not_now))
        }
    }
}

@Composable
private fun WatchVideoButton(
    videoState: StudyUnlockVideoState,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = videoState == StudyUnlockVideoState.READY,
        onClick = onClick,
    ) {
        if (videoState == StudyUnlockVideoState.LOADING || videoState == StudyUnlockVideoState.PLAYING) {
            CircularProgressIndicator(
                modifier = Modifier.size(progressSize),
                strokeWidth = progressStrokeWidth,
            )
        } else {
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Rounded.PlayCircle,
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = stringResource(Res.string.study_unlock_watch_video))
    }
}

@Composable
private fun videoCaption(uiState: StudyUnlockUiState): String = when (uiState.videoState) {
    StudyUnlockVideoState.LOADING -> stringResource(Res.string.study_unlock_video_loading)

    StudyUnlockVideoState.UNAVAILABLE -> stringResource(Res.string.study_unlock_video_unavailable)

    StudyUnlockVideoState.READY, StudyUnlockVideoState.PLAYING -> pluralStringResource(
        Res.plurals.study_unlock_remaining_today,
        uiState.rewardedRemainingToday,
        uiState.rewardedRemainingToday,
    )
}
