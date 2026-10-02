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

/**
 * On a wide window a study opens beside what it studies instead of covering it: the day beside its
 * study, and the chapter being read beside its chapter study.
 *
 * Going back leaves the day together with its study, which only came along with it, but closes just
 * the chapter study: the user opened it from the reader, so back must land on the chapter.
 */
class StudyPanelSceneStrategy(
    private val isWide: Boolean,
    private val readingFraction: Float,
    private val onReadingFractionCommit: (Float) -> Unit,
) : SceneStrategy<NavKey> {
    /** Each study pane, keyed to the pane it has to sit beside. */
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
            previousEntries = entries.dropLast(if (ChapterStudyPaneKey in detailEntry.metadata) 1 else 2),
            initialReadingFraction = readingFraction,
            onReadingFractionCommit = onReadingFractionCommit,
        )
    }
}
