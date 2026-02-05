package com.quare.bibleplanner.core.provider.room.converter

import androidx.room.TypeConverter
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus

class BibleVersionDownloadStatusConverter {
    @TypeConverter
    fun fromBibleVersionDownloadStatus(status: DownloadStatus): String = status.name

    @TypeConverter
    fun toBibleVersionDownloadStatus(name: String): DownloadStatus = DownloadStatus.valueOf(name)
}
