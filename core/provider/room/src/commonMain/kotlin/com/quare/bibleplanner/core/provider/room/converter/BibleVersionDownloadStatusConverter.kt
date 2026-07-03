package com.quare.bibleplanner.core.provider.room.converter

import androidx.room3.ColumnTypeConverter
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus

class BibleVersionDownloadStatusConverter {
    @ColumnTypeConverter
    fun fromBibleVersionDownloadStatus(status: DownloadStatus): String = status.name

    @ColumnTypeConverter
    fun toBibleVersionDownloadStatus(name: String): DownloadStatus = DownloadStatus.valueOf(name)
}
