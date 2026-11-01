package com.quare.bibleplanner.feature.read.presentation.screen

import com.quare.bibleplanner.feature.read.presentation.component.ListeningFollow
import com.quare.bibleplanner.feature.read.presentation.component.VerseFlash
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel

internal class ReadListBindings(
    val verseFlash: VerseFlash,
    val visibleChapter: ReadChapterUiModel?,
    val listeningFollow: ListeningFollow,
)
