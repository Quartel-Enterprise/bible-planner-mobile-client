package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "day_studies")
data class DayStudyEntity(
    @PrimaryKey val cacheKey: String,
    val passageLabel: String,
    val overview: String,
    val contextBody: String,
    val model: String,
    val promptVersion: Int,
    val updatedAt: String,
    val cacheToken: String,
)
