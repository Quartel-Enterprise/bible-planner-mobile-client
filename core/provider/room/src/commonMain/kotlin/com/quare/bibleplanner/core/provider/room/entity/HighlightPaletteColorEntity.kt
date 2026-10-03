package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "highlight_palette_colors")
data class HighlightPaletteColorEntity(
    @PrimaryKey val colorKey: String,
    val hue: Int,
    val lightness: Int,
    val createdAtEpochMillis: Long,
)
