package com.quare.bibleplanner.android.feature.read

import com.quare.bibleplanner.feature.read.presentation.component.VerseFlash
import com.quare.bibleplanner.feature.read.presentation.model.ReadChapterUiModel

internal class ReadListBindings(
    val verseFlash: VerseFlash,
    val visibleChapter: ReadChapterUiModel?,
    val listeningFollow: ListeningFollow,
)
