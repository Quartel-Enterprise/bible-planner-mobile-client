package com.quare.bibleplanner.core.verseannotations.domain.repository

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import kotlinx.coroutines.flow.Flow

interface HighlightPaletteRepository {
    fun observeCustomColors(): Flow<List<HighlightColor.Custom>>

    suspend fun add(color: HighlightColor.Custom)

    suspend fun remove(colorKey: String)
}
