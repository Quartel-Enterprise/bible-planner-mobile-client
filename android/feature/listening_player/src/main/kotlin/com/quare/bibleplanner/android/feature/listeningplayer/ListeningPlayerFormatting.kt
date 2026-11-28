package com.quare.bibleplanner.android.feature.listeningplayer

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.quare.bibleplanner.core.books.util.verseReferenceLabel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlin.time.Duration

private const val CLOCK_SECONDS_DIGITS = 2

internal class ListeningUnavailableCopy(
    val icon: ImageVector,
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
    @param:StringRes val action: Int?,
)

@Composable
internal fun chapterTitle(chapter: ChapterLocationModel): String = verseReference(
    chapter = chapter,
    verseNumber = null,
)

@Composable
internal fun verseReference(
    chapter: ChapterLocationModel,
    verseNumber: Int?,
): String = verseReferenceLabel(
    bookId = chapter.bookId,
    chapterNumber = chapter.chapterNumber,
    verseNumbers = listOfNotNull(verseNumber),
)

internal fun Duration.toClockText(): String = coerceAtLeast(Duration.ZERO).toComponents { minutes, seconds, _ ->
    "$minutes:${seconds.toString().padStart(CLOCK_SECONDS_DIGITS, '0')}"
}

/*
 * Why: the version title takes the version abbreviation as its argument; another version is the
 * only way out of a version without narration, so it offers no action.
 */
internal fun ListeningStatusModel.toUnavailableCopy(): ListeningUnavailableCopy? = when (this) {
    ListeningStatusModel.NEEDS_CONNECTION -> ListeningUnavailableCopy(
        icon = Icons.Rounded.CloudOff,
        title = R.string.listening_player_no_connection_title,
        body = R.string.listening_player_no_connection_body,
        action = R.string.listening_player_retry,
    )

    ListeningStatusModel.NEEDS_SIGN_IN -> ListeningUnavailableCopy(
        icon = Icons.Rounded.AccountCircle,
        title = R.string.listening_player_sign_in_title,
        body = R.string.listening_player_sign_in_body,
        action = R.string.listening_player_sign_in,
    )

    ListeningStatusModel.VERSION_UNAVAILABLE -> ListeningUnavailableCopy(
        icon = Icons.Rounded.VolumeOff,
        title = R.string.listening_player_version_unavailable_title,
        body = R.string.listening_player_version_unavailable_body,
        action = null,
    )

    ListeningStatusModel.AUDIO_UNAVAILABLE -> ListeningUnavailableCopy(
        icon = Icons.Rounded.ErrorOutline,
        title = R.string.listening_player_audio_unavailable_title,
        body = R.string.listening_player_audio_unavailable_body,
        action = R.string.listening_player_retry,
    )

    // Why: retrying can't help until the monthly generation budget resets, so there is no action.
    ListeningStatusModel.NARRATION_PAUSED -> ListeningUnavailableCopy(
        icon = Icons.Rounded.Schedule,
        title = R.string.listening_player_narration_paused_title,
        body = R.string.listening_player_narration_paused_body,
        action = null,
    )

    ListeningStatusModel.PREPARING,
    ListeningStatusModel.PLAYING,
    ListeningStatusModel.PAUSED,
    ListeningStatusModel.INTERRUPTED,
    ListeningStatusModel.NEXT_LOCKED,
    ListeningStatusModel.FINISHED,
    -> null
}
