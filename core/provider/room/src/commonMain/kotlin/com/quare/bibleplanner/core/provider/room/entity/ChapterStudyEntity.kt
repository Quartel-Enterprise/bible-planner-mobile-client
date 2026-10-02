package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chapter_studies")
data class ChapterStudyEntity(
    @PrimaryKey val cacheKey: String,
    val summary: String,
    val context: String,
    val keyVerseStart: Int?,
    val keyVerseEnd: Int?,
    val keyVerseNote: String?,
    val model: String,
    val promptVersion: Int,
    val updatedAt: String,
    val cacheToken: String,
)
