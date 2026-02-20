package com.quare.bibleplanner.core.books.domain.model

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel

data class BibleSelectionModel(
    val version: VersionModel,
    val status: DownloadStatusModel,
    val isSelected: Boolean,
)
