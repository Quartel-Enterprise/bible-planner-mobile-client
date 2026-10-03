package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "chapter_study_outline_sections",
    foreignKeys = [
        ForeignKey(
            entity = ChapterStudyEntity::class,
            parentColumns = ["cacheKey"],
            childColumns = ["cacheKey"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cacheKey")],
)
data class ChapterStudyOutlineSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val cacheKey: String,
    val position: Int,
    val startVerse: Int,
    val endVerse: Int,
    val title: String,
)
