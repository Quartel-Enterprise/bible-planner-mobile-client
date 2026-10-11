package com.quare.bibleplanner.feature.read.presentation.listening.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.VolumeOff
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
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningUnavailableCopy

/*
 * Why: the version title takes the version abbreviation as its argument; another version is the
 * only way out of a version without narration, so it offers no action.
 */
internal fun ListeningStatusModel.toUnavailableCopy(): ListeningUnavailableCopy? = when (this) {
    ListeningStatusModel.NEEDS_CONNECTION -> ListeningUnavailableCopy(
        icon = Icons.Rounded.CloudOff,
        title = Res.string.listening_no_connection_title,
        body = Res.string.listening_no_connection_body,
        action = Res.string.listening_retry,
    )

    ListeningStatusModel.NEEDS_SIGN_IN -> ListeningUnavailableCopy(
        icon = Icons.Rounded.AccountCircle,
        title = Res.string.listening_sign_in_title,
        body = Res.string.listening_sign_in_body,
        action = Res.string.listening_sign_in,
    )

    ListeningStatusModel.VERSION_UNAVAILABLE -> ListeningUnavailableCopy(
        icon = Icons.Rounded.VolumeOff,
        title = Res.string.listening_version_unavailable_title,
        body = Res.string.listening_version_unavailable_body,
        action = null,
    )

    ListeningStatusModel.AUDIO_UNAVAILABLE -> ListeningUnavailableCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = Res.string.listening_audio_unavailable_title,
        body = Res.string.listening_audio_unavailable_body,
        action = Res.string.listening_retry,
    )

    ListeningStatusModel.PREPARING,
    ListeningStatusModel.PLAYING,
    ListeningStatusModel.PAUSED,
    ListeningStatusModel.INTERRUPTED,
    ListeningStatusModel.NEXT_LOCKED,
    ListeningStatusModel.FINISHED,
    -> null
}
