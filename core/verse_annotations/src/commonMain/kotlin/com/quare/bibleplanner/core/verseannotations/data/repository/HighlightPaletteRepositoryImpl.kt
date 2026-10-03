package com.quare.bibleplanner.core.verseannotations.data.repository

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.room.dao.HighlightPaletteColorDao
import com.quare.bibleplanner.core.provider.room.entity.HighlightPaletteColorEntity
import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HighlightPaletteRepositoryImpl(
    private val highlightPaletteColorDao: HighlightPaletteColorDao,
    private val currentTimestampProvider: CurrentTimestampProvider,
) : HighlightPaletteRepository {
    override fun observeCustomColors(): Flow<List<HighlightColor.Custom>> = highlightPaletteColorDao
        .getPaletteColorsFlow()
        .map { entities ->
            entities.map { entity ->
                HighlightColor.Custom(
                    hue = entity.hue,
                    lightness = entity.lightness,
                )
            }
        }

    // Why: keeps only the latest MAX_CUSTOM_COLORS mixes; the row is a shortcut, not a record, and
    // highlights with an evicted colour still render since the components live in their own key.
    override suspend fun add(color: HighlightColor.Custom) {
        highlightPaletteColorDao.upsertPaletteColor(
            HighlightPaletteColorEntity(
                colorKey = color.key,
                hue = color.hue,
                lightness = color.lightness,
                createdAtEpochMillis = currentTimestampProvider.getCurrentTimestamp(),
            ),
        )
        highlightPaletteColorDao
            .getPaletteColors()
            .dropLast(MAX_CUSTOM_COLORS)
            .forEach { evicted -> highlightPaletteColorDao.deletePaletteColor(evicted.colorKey) }
    }

    override suspend fun remove(colorKey: String) {
        highlightPaletteColorDao.deletePaletteColor(colorKey)
    }

    private companion object {
        const val MAX_CUSTOM_COLORS = 4
    }
}
