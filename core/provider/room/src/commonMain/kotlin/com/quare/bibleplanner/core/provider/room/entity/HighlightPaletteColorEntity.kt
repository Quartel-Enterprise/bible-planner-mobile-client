package com.quare.bibleplanner.core.provider.room.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A custom colour the user mixed, kept so it stays offered in the palette after being applied.
 *
 * Device-local on purpose: [VerseHighlightEntity.color] carries the full colour key, so a highlight
 * made with a custom colour renders correctly on a device that never had it in its palette.
 */
@Entity(tableName = "highlight_palette_colors")
data class HighlightPaletteColorEntity(
    @PrimaryKey val colorKey: String,
    val hue: Int,
    val lightness: Int,
    val createdAtEpochMillis: Long,
)
