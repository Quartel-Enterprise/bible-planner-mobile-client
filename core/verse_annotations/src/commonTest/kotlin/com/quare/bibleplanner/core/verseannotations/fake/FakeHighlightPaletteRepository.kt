package com.quare.bibleplanner.core.verseannotations.fake

import com.quare.bibleplanner.core.verseannotations.domain.model.HighlightColor
import com.quare.bibleplanner.core.verseannotations.domain.repository.HighlightPaletteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeHighlightPaletteRepository(
    initialColors: List<HighlightColor.Custom> = emptyList(),
) : HighlightPaletteRepository {
    val customColors = MutableStateFlow(initialColors)

    override fun observeCustomColors(): Flow<List<HighlightColor.Custom>> = customColors

    override suspend fun add(color: HighlightColor.Custom) {
        customColors.value = customColors.value.filterNot { it.key == color.key } + color
    }

    override suspend fun remove(colorKey: String) {
        customColors.value = customColors.value.filterNot { it.key == colorKey }
    }
}
