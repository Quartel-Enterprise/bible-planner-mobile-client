package com.quare.bibleplanner.feature.bibleversion.fake

import com.quare.bibleplanner.core.books.domain.model.BibleModel
import com.quare.bibleplanner.core.books.domain.model.VersionModel
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel
import com.quare.bibleplanner.core.utils.locale.Language

internal fun bibleModel(
    id: String,
    language: Language = Language.PORTUGUESE_BRAZIL,
    isSelected: Boolean = false,
    hasPendingUpdate: Boolean = false,
): BibleModel = BibleModel(
    version = VersionModel(
        id = id,
        name = "Version $id",
        version = "1.0.0",
        language = language,
        chapters = 1189,
        size = 8_000_000L,
    ),
    downloadedChapters = 1189,
    downloadStatus = DownloadStatusModel.Downloaded,
    isSelected = isSelected,
    hasPendingUpdate = hasPendingUpdate,
)
