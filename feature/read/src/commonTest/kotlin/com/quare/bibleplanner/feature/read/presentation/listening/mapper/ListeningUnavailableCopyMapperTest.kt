package com.quare.bibleplanner.feature.read.presentation.listening.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VolumeOff
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_audio_unavailable_body
import bibleplanner.feature.read.generated.resources.listening_audio_unavailable_title
import bibleplanner.feature.read.generated.resources.listening_narration_paused_body
import bibleplanner.feature.read.generated.resources.listening_narration_paused_title
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
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ListeningUnavailableCopyMapperTest {
    @Test
    fun `GIVEN each status WHEN mapping its unavailable copy THEN only the audio failures get one`() {
        // Given
        val statuses = ListeningStatusModel.entries

        // When
        val copies = statuses.associateWith { status -> status.toUnavailableCopy() }

        // Then
        assertEquals(
            mapOf(
                ListeningStatusModel.PREPARING to null,
                ListeningStatusModel.PLAYING to null,
                ListeningStatusModel.PAUSED to null,
                ListeningStatusModel.INTERRUPTED to null,
                ListeningStatusModel.NEEDS_CONNECTION to ListeningUnavailableCopy(
                    icon = Icons.Rounded.CloudOff,
                    title = Res.string.listening_no_connection_title,
                    body = Res.string.listening_no_connection_body,
                    action = Res.string.listening_retry,
                ),
                ListeningStatusModel.NEEDS_SIGN_IN to ListeningUnavailableCopy(
                    icon = Icons.Rounded.AccountCircle,
                    title = Res.string.listening_sign_in_title,
                    body = Res.string.listening_sign_in_body,
                    action = Res.string.listening_sign_in,
                ),
                ListeningStatusModel.VERSION_UNAVAILABLE to ListeningUnavailableCopy(
                    icon = Icons.Rounded.VolumeOff,
                    title = Res.string.listening_version_unavailable_title,
                    body = Res.string.listening_version_unavailable_body,
                    action = null,
                ),
                ListeningStatusModel.AUDIO_UNAVAILABLE to ListeningUnavailableCopy(
                    icon = Icons.Rounded.ErrorOutline,
                    title = Res.string.listening_audio_unavailable_title,
                    body = Res.string.listening_audio_unavailable_body,
                    action = Res.string.listening_retry,
                ),
                ListeningStatusModel.NARRATION_PAUSED to ListeningUnavailableCopy(
                    icon = Icons.Rounded.Schedule,
                    title = Res.string.listening_narration_paused_title,
                    body = Res.string.listening_narration_paused_body,
                    action = null,
                ),
                ListeningStatusModel.NEXT_LOCKED to null,
                ListeningStatusModel.FINISHED to null,
            ),
            copies,
        )
    }
}
