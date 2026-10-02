package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "chapter_study_cross_references",
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
data class ChapterStudyCrossReferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cacheKey: String,
    val position: Int,
    val bookId: String,
    val chapterNumber: Int,
    val startVerse: Int,
    val endVerse: Int,
)
