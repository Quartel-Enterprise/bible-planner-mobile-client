package com.quare.bibleplanner.feature.read.fixture

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningStatusModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningFinishOfferUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningPlayerUiModel
import com.quare.bibleplanner.feature.read.presentation.listening.model.ReadListeningUiState
import kotlin.time.Duration.Companion.seconds

internal fun hiddenListeningUiState(): ReadListeningUiState = ReadListeningUiState(
    isAvailable = false,
    speed = 1f,
    todayChapters = emptyList(),
    player = null,
    finishOffer = null,
)

internal fun availableListeningUiState(
    player: ListeningPlayerUiModel? = null,
    finishOffer: ListeningFinishOfferUiModel? = null,
    todayChapters: List<ChapterLocationModel> = emptyList(),
): ReadListeningUiState = ReadListeningUiState(
    isAvailable = true,
    speed = 1f,
    todayChapters = todayChapters,
    player = player,
    finishOffer = finishOffer,
)

internal fun listeningPlayer(
    chapter: ChapterLocationModel = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1),
    status: ListeningStatusModel = ListeningStatusModel.PLAYING,
    verseNumber: Int? = 2,
    lockedChapter: ChapterLocationModel? = null,
): ListeningPlayerUiModel = ListeningPlayerUiModel(
    chapter = chapter,
    verseNumber = verseNumber,
    verseText = "And the earth was without form, and void.",
    verseIndex = 1,
    verseCount = 31,
    status = status,
    progress = 0.05f,
    elapsed = 12.seconds,
    remaining = 228.seconds,
    total = 240.seconds,
    versionAbbreviation = "WEB",
    languageTag = "en-US",
    dayProgress = null,
    lockedChapter = lockedChapter,
)
