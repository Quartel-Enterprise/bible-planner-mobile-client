package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.ReaderPaneKey
import com.quare.bibleplanner.core.model.route.VerseSelectionPaneKey
import com.quare.bibleplanner.core.navigation.scene.VerseSelectionScene

class VerseSelectionSceneStrategy(
    private val isWide: Boolean,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        val selectionEntry = entries.lastOrNull() ?: return null
        val readerEntry = entries.getOrNull(entries.lastIndex - 1) ?: return null
        if (VerseSelectionPaneKey !in selectionEntry.metadata) return null
        if (ReaderPaneKey !in readerEntry.metadata) return null
        return VerseSelectionScene(
            key = readerEntry.contentKey,
            readerEntry = readerEntry,
            selectionEntry = selectionEntry,
            /*
             * Only the panel is dropped: it sits over the reader rather than replacing it, so going
             * back from here must land on the chapter, not on whatever opened it.
             */
            previousEntries = entries.dropLast(1),
            isWide = isWide,
        )
    }
}
