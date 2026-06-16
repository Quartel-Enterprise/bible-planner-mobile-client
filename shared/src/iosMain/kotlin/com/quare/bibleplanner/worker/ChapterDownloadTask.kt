package com.quare.bibleplanner.worker

internal data class ChapterDownloadTask(
    val url: String,
    val versionId: String,
    val chapterId: Long,
)
