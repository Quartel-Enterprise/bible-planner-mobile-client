package com.quare.bibleplanner.core.books.domain.model

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel

data class BibleModel(
    val version: VersionModel,
    val downloadStatus: DownloadStatusModel,
    val isSelected: Boolean,
)
