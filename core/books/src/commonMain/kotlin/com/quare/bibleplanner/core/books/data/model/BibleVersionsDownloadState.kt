package com.quare.bibleplanner.core.books.data.model

import com.quare.bibleplanner.core.provider.room.entity.BibleVersionEntity
import com.quare.bibleplanner.core.provider.room.relation.VersionChapterCount

internal data class BibleVersionsDownloadState(
    val versions: List<BibleVersionEntity>,
    val chapterCounts: List<VersionChapterCount>,
)
