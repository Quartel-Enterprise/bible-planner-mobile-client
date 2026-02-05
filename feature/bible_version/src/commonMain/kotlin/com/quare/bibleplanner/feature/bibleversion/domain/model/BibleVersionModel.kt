package com.quare.bibleplanner.feature.bibleversion.domain.model

import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatusModel

data class BibleVersionModel(
    val id: String,
    val name: String,
    val status: DownloadStatusModel,
)
