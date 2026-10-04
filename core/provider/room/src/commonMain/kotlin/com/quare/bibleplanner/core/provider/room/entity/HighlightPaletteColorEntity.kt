package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/*
 * Why: device-local on purpose; VerseHighlightEntity.color carries the full colour key,
 * so highlights render on devices that never had the custom colour.
 */
@Entity(tableName = "highlight_palette_colors")
data class HighlightPaletteColorEntity(
    @PrimaryKey val colorKey: String,
    val hue: Int,
    val lightness: Int,
    val createdAtEpochMillis: Long,
)
