package com.quare.bibleplanner.core.navigation.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

// Why: the reader stays visible under the selection panel because the selection is built by
// tapping verses; a modal sheet covering them would end that interaction.
internal class VerseSelectionScene(
    override val key: Any,
    private val readerEntry: NavEntry<NavKey>,
    private val selectionEntry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
    private val isWide: Boolean,
) : Scene<NavKey> {
    private val panelWidth = 392.dp
    override val entries: List<NavEntry<NavKey>> = listOf(readerEntry, selectionEntry)

    override val content: @Composable () -> Unit = {
        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    readerEntry.Content()
                }
                VerticalDivider()
                Box(modifier = Modifier.width(panelWidth).fillMaxHeight()) {
                    selectionEntry.Content()
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                readerEntry.Content()
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    selectionEntry.Content()
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VerseSelectionScene

        return key == other.key &&
            readerEntry == other.readerEntry &&
            selectionEntry == other.selectionEntry &&
            isWide == other.isWide &&
            previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = key.hashCode() * 31 +
        readerEntry.hashCode() * 31 +
        selectionEntry.hashCode() * 31 +
        isWide.hashCode() * 31 +
        previousEntries.hashCode() * 31

    override fun toString(): String = "VerseSelectionScene(key=$key, readerEntry=$readerEntry, " +
        "selectionEntry=$selectionEntry, isWide=$isWide, previousEntries=$previousEntries)"
}
