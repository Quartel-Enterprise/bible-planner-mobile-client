package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.ChapterStudyPaneKey
import com.quare.bibleplanner.core.model.route.ReaderPaneKey
import com.quare.bibleplanner.core.model.route.VerseSelectionPaneKey
import com.quare.bibleplanner.core.navigation.scene.VerseSelectionScene

class VerseSelectionSceneStrategy(
    private val isWide: Boolean,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        val selectionEntry = entries.lastOrNull() ?: return null
        if (VerseSelectionPaneKey !in selectionEntry.metadata) return null
        val readerEntry = entries.findReaderUnderSelection() ?: return null
        return VerseSelectionScene(
            key = readerEntry.contentKey,
            readerEntry = readerEntry,
            selectionEntry = selectionEntry,
            previousEntries = entries.dropLast(1),
            isWide = isWide,
        )
    }

    private fun List<NavEntry<NavKey>>.findReaderUnderSelection(): NavEntry<NavKey>? {
        val underSelection = getOrNull(lastIndex - 1) ?: return null
        val candidate = if (ChapterStudyPaneKey in underSelection.metadata) {
            getOrNull(lastIndex - 2) ?: return null
        } else {
            underSelection
        }
        return candidate.takeIf { ReaderPaneKey in it.metadata }
    }
}
