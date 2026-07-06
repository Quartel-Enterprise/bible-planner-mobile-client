package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "day_study_takeaways",
    foreignKeys = [
        ForeignKey(
            entity = DayStudyEntity::class,
            parentColumns = ["cacheKey"],
            childColumns = ["cacheKey"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("cacheKey")],
)
data class DayStudyTakeawayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cacheKey: String,
    val position: Int,
    val text: String,
)
