package com.quare.bibleplanner.core.provider.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_versions")
data class BibleVersionEntity(
    @PrimaryKey val id: String,
    val downloadProgress: Float,
    val status: BibleVersionDownloadStatus,
)
