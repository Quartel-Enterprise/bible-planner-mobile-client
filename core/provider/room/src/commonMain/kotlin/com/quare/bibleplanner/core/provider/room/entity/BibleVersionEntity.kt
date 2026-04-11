package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quare.bibleplanner.core.model.downloadstatus.DownloadStatus

@Entity(tableName = "bible_versions")
data class BibleVersionEntity(
    @PrimaryKey val id: String,
    val status: DownloadStatus,
    @ColumnInfo(defaultValue = "1189") val totalChapters: Int = 1189,
)
