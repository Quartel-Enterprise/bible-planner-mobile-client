package com.quare.bibleplanner.core.provider.room.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightPaletteColorDao {
    @Query("SELECT * FROM highlight_palette_colors ORDER BY createdAtEpochMillis")
    fun getPaletteColorsFlow(): Flow<List<HighlightPaletteColorEntity>>

    @Query("SELECT * FROM highlight_palette_colors ORDER BY createdAtEpochMillis")
    suspend fun getPaletteColors(): List<HighlightPaletteColorEntity>

    @Upsert
    suspend fun upsertPaletteColor(color: HighlightPaletteColorEntity)

    @Query("DELETE FROM highlight_palette_colors WHERE colorKey = :colorKey")
    suspend fun deletePaletteColor(colorKey: String)

    @Query("DELETE FROM highlight_palette_colors")
    suspend fun deleteAllPaletteColors()
}
