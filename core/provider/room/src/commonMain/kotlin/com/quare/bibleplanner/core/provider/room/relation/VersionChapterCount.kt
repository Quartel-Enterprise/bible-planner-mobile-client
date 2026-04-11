package com.quare.bibleplanner.core.provider.room.relation

data class VersionChapterCount(
    val bibleVersionId: String,
    val downloadedChapters: Int,
)
