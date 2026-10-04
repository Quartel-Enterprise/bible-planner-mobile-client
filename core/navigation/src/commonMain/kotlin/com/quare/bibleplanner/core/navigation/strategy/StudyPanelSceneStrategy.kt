package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.ChapterStudyPaneKey
import com.quare.bibleplanner.core.model.route.DayStudyDetailPaneKey
import com.quare.bibleplanner.core.model.route.DayStudyMainPaneKey
import com.quare.bibleplanner.core.model.route.ReaderPaneKey
import com.quare.bibleplanner.core.navigation.scene.StudyPanelScene

/*
 * Why: on wide windows a study sits beside what it studies instead of covering it; the study only
 * came along with its pane, so going back leaves both.
 */
class StudyPanelSceneStrategy(
    private val isWide: Boolean,
    private val readingFraction: Float,
    private val onReadingFractionCommit: (Float) -> Unit,
) : SceneStrategy<NavKey> {
    private val mainPaneKeys: Map<NavMetadataKey<Boolean>, NavMetadataKey<Boolean>> = mapOf(
        DayStudyDetailPaneKey to DayStudyMainPaneKey,
        ChapterStudyPaneKey to ReaderPaneKey,
    )

    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!isWide) return null
        val detailEntry = entries.lastOrNull() ?: return null
        val mainEntry = entries.getOrNull(entries.lastIndex - 1) ?: return null
        val mainPaneKey = mainPaneKeys.entries
            .firstOrNull { (detailPaneKey, _) -> detailPaneKey in detailEntry.metadata }
            ?.value ?: return null
        if (mainPaneKey !in mainEntry.metadata) return null
        return StudyPanelScene(
            key = mainEntry.contentKey,
            mainEntry = mainEntry,
            detailEntry = detailEntry,
            previousEntries = entries.dropLast(2),
            initialReadingFraction = readingFraction,
            onReadingFractionCommit = onReadingFractionCommit,
        )
    }
}
