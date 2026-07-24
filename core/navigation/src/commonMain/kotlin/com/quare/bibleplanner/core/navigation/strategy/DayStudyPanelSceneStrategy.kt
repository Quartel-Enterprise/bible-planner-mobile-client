package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.DayStudyDetailPaneKey
import com.quare.bibleplanner.core.model.route.DayStudyMainPaneKey
import com.quare.bibleplanner.core.navigation.scene.DayStudyPanelScene

class DayStudyPanelSceneStrategy(
    private val isWide: Boolean,
    private val readingFraction: Float,
    private val onReadingFractionCommit: (Float) -> Unit,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!isWide) return null
        val detailEntry = entries.lastOrNull() ?: return null
        val mainEntry = entries.getOrNull(entries.lastIndex - 1) ?: return null
        if (DayStudyDetailPaneKey !in detailEntry.metadata) return null
        if (DayStudyMainPaneKey !in mainEntry.metadata) return null
        return DayStudyPanelScene(
            key = mainEntry.contentKey,
            mainEntry = mainEntry,
            detailEntry = detailEntry,
            previousEntries = entries.dropLast(2),
            initialReadingFraction = readingFraction,
            onReadingFractionCommit = onReadingFractionCommit,
        )
    }
}
