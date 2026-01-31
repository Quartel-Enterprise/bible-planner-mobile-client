package com.quare.bibleplanner.core.provider.room.converter

import androidx.room.TypeConverter
import com.quare.bibleplanner.core.provider.room.entity.BibleVersionDownloadStatus

class BibleVersionDownloadStatusConverter {
    @TypeConverter
    fun fromBibleVersionDownloadStatus(status: BibleVersionDownloadStatus): String = status.name

    @TypeConverter
    fun toBibleVersionDownloadStatus(name: String): BibleVersionDownloadStatus =
        BibleVersionDownloadStatus.valueOf(name)
}
