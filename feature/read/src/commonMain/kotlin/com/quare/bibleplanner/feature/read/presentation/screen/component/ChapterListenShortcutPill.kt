package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bibleplanner.feature.read.generated.resources.Res
import bibleplanner.feature.read.generated.resources.listening_listen_chapter
import bibleplanner.feature.read.generated.resources.listening_listen_today
import bibleplanner.feature.read.generated.resources.listening_listening_remaining
import bibleplanner.feature.read.generated.resources.listening_paused_at
import bibleplanner.feature.read.generated.resources.listening_preparing
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.presentation.listening.getListeningMinutes
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningEntrySource
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.listening.toClockText
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel
import org.jetbrains.compose.resources.stringResource

internal const val LISTEN_SHORTCUT_TAG = "listen_shortcut"
private const val SOFT_PRIMARY_ALPHA = 0.12f
private val pillHeight = 38.dp
private val iconSize = 18.dp

@Composable
internal fun ChapterListenShortcutPill(
    chapter: ReadChapterUiModel,
    listening: ReadListeningUiState,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val location = remember(chapter.chapter) {
        ChapterLocationModel(
            bookId = chapter.chapter.bookId,
            chapterNumber = chapter.chapter.chapterNumber,
        )
    }
    val player = listening.player?.takeIf { it.chapter == location }
    val isActive = player != null
    val todayPosition = listening.todayChapters.indexOf(location)
    val minutes = remember(chapter.verses, listening.speed) {
        getListeningMinutes(
            verseTexts = chapter.verses.map { it.text },
            speed = listening.speed,
        )
    }
    val label = when {
        player == null && todayPosition >= 0 -> stringResource(
            Res.string.listening_listen_today,
            todayPosition + 1,
            listening.todayChapters.size,
        )

        player == null -> stringResource(Res.string.listening_listen_chapter, minutes)

        player.status == ListeningStatusModel.PREPARING -> stringResource(Res.string.listening_preparing)

        player.status == ListeningStatusModel.PLAYING -> stringResource(
            Res.string.listening_listening_remaining,
            player.remaining.toClockText(),
        )

        else -> stringResource(
            Res.string.listening_paused_at,
            verseReference(
                chapter = location,
                verseNumber = player.verseNumber,
            ),
        )
    }
    val icon: ImageVector = when {
        player == null && todayPosition >= 0 -> Icons.AutoMirrored.Rounded.PlaylistPlay
        player == null -> Icons.Rounded.Headphones
        player.status == ListeningStatusModel.PLAYING -> Icons.Rounded.GraphicEq
        else -> Icons.Rounded.PauseCircle
    }
    Box(
        modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            modifier = Modifier.height(pillHeight).testTag(LISTEN_SHORTCUT_TAG),
            onClick = {
                onListeningEvent(
                    ReadListeningUiEvent.OnListenClick(
                        chapter = location,
                        source = ListeningEntrySource.SHORTCUT,
                    ),
                )
            },
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = SOFT_PRIMARY_ALPHA)
                },
                contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            ),
            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = icon,
                    contentDescription = null,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
