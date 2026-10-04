package com.quare.bibleplanner.core.navigation.strategy

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.quare.bibleplanner.core.model.route.SheetPaneKey
import com.quare.bibleplanner.core.navigation.scene.SheetScene
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimation

class SheetSceneStrategy : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        val sheetEntry = entries.lastOrNull() ?: return null
        if (SheetPaneKey !in sheetEntry.metadata) return null
        return SheetScene(
            key = sheetEntry.contentKey,
            entry = sheetEntry,
            previousEntries = entries.dropLast(1),
            exitAnimation = SheetExitAnimation(),
        )
    }
}
