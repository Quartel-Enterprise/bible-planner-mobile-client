package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.feature.read.presentation.component.ListeningFollow
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiEvent
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent

@Composable
internal fun ListeningOverlay(
    listening: ReadListeningUiState,
    follow: ListeningFollow,
    onEvent: (ReadUiEvent) -> Unit,
    onListeningEvent: (ReadListeningUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val finishOffer = listening.finishOffer
    val player = listening.player
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (finishOffer != null) {
            ListeningFinishOfferCard(
                offer = finishOffer,
                onMarkReadClick = {
                    onEvent(
                        ReadUiEvent.OnListeningMarkReadClick(
                            bookId = finishOffer.chapter.bookId,
                            chapterNumber = finishOffer.chapter.chapterNumber,
                        ),
                    )
                },
                onDismissClick = { onListeningEvent(ReadListeningUiEvent.OnFinishOfferDismissClick) },
            )
        } else if (follow.isShowingBackToVerse && player != null) {
            ListeningBackToVerseChip(
                verseLabel = "${player.chapter.chapterNumber}:${player.verseNumber}",
                isVerseAbove = follow.isVerseAbove,
                onClick = {
                    follow.backToVerse()
                    onListeningEvent(ReadListeningUiEvent.OnBackToVerseClick)
                },
            )
        }
    }
}
