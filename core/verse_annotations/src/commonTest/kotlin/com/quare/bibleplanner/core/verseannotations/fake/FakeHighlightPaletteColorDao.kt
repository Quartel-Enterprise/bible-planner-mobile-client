package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.provider.room.dao.HighlightPaletteColorDao
import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeHighlightPaletteColorDao(
    initialColors: List<HighlightPaletteColorEntity> = emptyList(),
) : HighlightPaletteColorDao {
    val colors = MutableStateFlow(initialColors)

    override fun getPaletteColorsFlow(): Flow<List<HighlightPaletteColorEntity>> =
        colors.map { current -> current.sortedBy { it.createdAtEpochMillis } }

    override suspend fun getPaletteColors(): List<HighlightPaletteColorEntity> =
        colors.value.sortedBy { it.createdAtEpochMillis }

    override suspend fun upsertPaletteColor(color: HighlightPaletteColorEntity) {
        colors.value = colors.value.filterNot { it.colorKey == color.colorKey } + color
    }

    override suspend fun deletePaletteColor(colorKey: String) {
        colors.value = colors.value.filterNot { it.colorKey == colorKey }
    }

    override suspend fun deleteAllPaletteColors() {
        colors.value = emptyList()
    }
}
