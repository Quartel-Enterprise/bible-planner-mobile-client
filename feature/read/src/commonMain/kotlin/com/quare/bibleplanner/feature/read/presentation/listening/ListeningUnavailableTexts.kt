package com.quare.bibleplanner.feature.read.presentation.listening

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_audio_unavailable_body
import bibleplanner.feature.read.generated.resources.listening_audio_unavailable_title
import bibleplanner.feature.read.generated.resources.listening_no_connection_body
import bibleplanner.feature.read.generated.resources.listening_no_connection_title
import bibleplanner.feature.read.generated.resources.listening_retry
import bibleplanner.feature.read.generated.resources.listening_sign_in
import bibleplanner.feature.read.generated.resources.listening_sign_in_body
import bibleplanner.feature.read.generated.resources.listening_sign_in_title
import bibleplanner.feature.read.generated.resources.listening_version_unavailable_body
import bibleplanner.feature.read.generated.resources.listening_version_unavailable_title
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import org.jetbrains.compose.resources.stringResource

internal val ListeningStatusModel.isUnavailable: Boolean
    get() = this == ListeningStatusModel.NEEDS_CONNECTION ||
        this == ListeningStatusModel.NEEDS_SIGN_IN ||
        this == ListeningStatusModel.VERSION_UNAVAILABLE ||
        this == ListeningStatusModel.AUDIO_UNAVAILABLE

// Why: another version is the only way out of a version without narration, so it offers no action.
internal val ListeningStatusModel.hasUnavailableAction: Boolean
    get() = isUnavailable && this != ListeningStatusModel.VERSION_UNAVAILABLE

internal fun ListeningStatusModel.toUnavailableIcon(): ImageVector = when (this) {
    ListeningStatusModel.NEEDS_CONNECTION -> Icons.Rounded.CloudOff
    ListeningStatusModel.NEEDS_SIGN_IN -> Icons.Rounded.AccountCircle
    ListeningStatusModel.VERSION_UNAVAILABLE -> Icons.Rounded.VolumeOff
    else -> Icons.Rounded.ErrorOutline
}

@Composable
internal fun listeningUnavailableTitle(player: ListeningPlayerUiModel): String = when (player.status) {
    ListeningStatusModel.NEEDS_CONNECTION -> stringResource(Res.string.listening_no_connection_title)

    ListeningStatusModel.NEEDS_SIGN_IN -> stringResource(Res.string.listening_sign_in_title)

    ListeningStatusModel.VERSION_UNAVAILABLE ->
        stringResource(Res.string.listening_version_unavailable_title, player.versionAbbreviation)

    else -> stringResource(Res.string.listening_audio_unavailable_title)
}

@Composable
internal fun listeningUnavailableBody(status: ListeningStatusModel): String = when (status) {
    ListeningStatusModel.NEEDS_CONNECTION -> stringResource(Res.string.listening_no_connection_body)
    ListeningStatusModel.NEEDS_SIGN_IN -> stringResource(Res.string.listening_sign_in_body)
    ListeningStatusModel.VERSION_UNAVAILABLE -> stringResource(Res.string.listening_version_unavailable_body)
    else -> stringResource(Res.string.listening_audio_unavailable_body)
}

@Composable
internal fun listeningUnavailableAction(status: ListeningStatusModel): String = if (status ==
    ListeningStatusModel.NEEDS_SIGN_IN
) {
    stringResource(Res.string.listening_sign_in)
} else {
    stringResource(Res.string.listening_retry)
}
