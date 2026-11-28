package com.quare.bibleplanner.android.feature.studyunlock

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.android.designsystem.icon.SheetIconTile
import com.quare.bibleplanner.core.model.route.StudyUnlockSurface
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiEvent
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockUiState
import com.quare.bibleplanner.feature.studyunlock.presentation.model.StudyUnlockVideoState
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimationEffect
import com.quare.bibleplanner.ui.utils.sheet.blockPointerInput
import com.quare.bibleplanner.ui.utils.sheet.rememberSheetCloseGuard

private val bodyMaxWidth = 300.dp
private val progressSize = 18.dp
private val progressStrokeWidth = 2.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudyUnlockContent(
    uiState: StudyUnlockUiState,
    onEvent: (StudyUnlockUiEvent) -> Unit,
) {
    val isPlaying = uiState.videoState == StudyUnlockVideoState.PLAYING
    val currentIsPlaying by rememberUpdatedState(isPlaying)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        /*
         * Why: back and a scrim tap still dismiss with the gestures off, and a pop while the video plays would
         * lose the reward, or pop the screen below too once the earned reward pops the sheet again.
         */
        confirmValueChange = remember { { value: SheetValue -> value != SheetValue.Hidden || !currentIsPlaying } },
    )
    val sheetCloseGuard = rememberSheetCloseGuard()
    val onDismiss = { sheetCloseGuard.close { onEvent(StudyUnlockUiEvent.OnDismiss) } }
    SheetExitAnimationEffect(
        animateOut = sheetState::hide,
        animateBackIn = sheetState::show,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !sheetCloseGuard.isClosing && !isPlaying,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .blockPointerInput(isBlocked = sheetCloseGuard.isClosing)
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ).verticalScroll(rememberScrollState()),
        ) {
            SheetIconTile(imageVector = Icons.Rounded.LockOpen)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(uiState.surface.toTitle()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.widthIn(max = bodyMaxWidth),
                text = stringResource(uiState.surface.toBody()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isPlaying,
                onClick = { sheetCloseGuard.close { onEvent(StudyUnlockUiEvent.OnSubscribeClick) } },
            ) {
                ButtonIcon(imageVector = Icons.Rounded.WorkspacePremium)
                Text(text = stringResource(R.string.study_unlock_subscribe))
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isFreeUnlockShown) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { sheetCloseGuard.close { onEvent(StudyUnlockUiEvent.OnUnlockWithoutVideoClick) } },
                ) {
                    ButtonIcon(imageVector = Icons.Rounded.Headphones)
                    Text(text = stringResource(R.string.study_unlock_listen_free))
                }
            } else {
                WatchVideoButton(
                    videoState = uiState.videoState,
                    onClick = { onEvent(StudyUnlockUiEvent.OnWatchVideoClick) },
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = videoCaption(uiState),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(
                enabled = !isPlaying,
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.study_unlock_not_now))
            }
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
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        } else {
            ButtonIcon(imageVector = Icons.Rounded.PlayCircle)
        }
        Text(text = stringResource(R.string.study_unlock_watch_video))
    }
}

@Composable
private fun ButtonIcon(imageVector: ImageVector) {
    Icon(
        modifier = Modifier.size(ButtonDefaults.IconSize),
        imageVector = imageVector,
        contentDescription = null,
    )
    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
}

@Composable
private fun videoCaption(uiState: StudyUnlockUiState): String = when (uiState.videoState) {
    StudyUnlockVideoState.LOADING -> stringResource(R.string.study_unlock_video_loading)

    StudyUnlockVideoState.UNAVAILABLE -> if (uiState.canUnlockWithoutVideo) {
        stringResource(R.string.study_unlock_video_unavailable_free)
    } else {
        stringResource(R.string.study_unlock_video_unavailable)
    }

    StudyUnlockVideoState.READY, StudyUnlockVideoState.PLAYING -> pluralStringResource(
        R.plurals.study_unlock_remaining_today,
        uiState.rewardedRemainingToday,
        uiState.rewardedRemainingToday,
    )
}

@StringRes
private fun StudyUnlockSurface.toTitle(): Int = if (this == StudyUnlockSurface.CHAPTER_LISTENING) {
    R.string.study_unlock_listening_title
} else {
    R.string.study_unlock_title
}

@StringRes
private fun StudyUnlockSurface.toBody(): Int = if (this == StudyUnlockSurface.CHAPTER_LISTENING) {
    R.string.study_unlock_listening_body
} else {
    R.string.study_unlock_body
}
