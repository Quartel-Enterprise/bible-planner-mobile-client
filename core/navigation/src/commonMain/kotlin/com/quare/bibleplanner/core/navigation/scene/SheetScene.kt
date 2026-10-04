package com.quare.bibleplanner.core.navigation.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import com.quare.bibleplanner.ui.utils.sheet.LocalSheetExitAnimation
import com.quare.bibleplanner.ui.utils.sheet.SheetExitAnimation

// Why: a dialog scene leaves composition the moment its entry is popped, so a sheet closed by
// anything but a swipe vanished at once; this one stays until the sheet has slid away.
internal class SheetScene(
    override val key: Any,
    private val entry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
    private val exitAnimation: SheetExitAnimation,
) : OverlayScene<NavKey> {
    override val entries: List<NavEntry<NavKey>> = listOf(entry)
    override val overlaidEntries: List<NavEntry<NavKey>> = previousEntries

    override val content: @Composable () -> Unit = {
        CompositionLocalProvider(LocalSheetExitAnimation provides exitAnimation) {
            entry.Content()
        }
    }

    override suspend fun onRemove() {
        exitAnimation.play()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SheetScene

        return key == other.key &&
            entry == other.entry &&
            previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = key.hashCode() * 31 +
        entry.hashCode() * 31 +
        previousEntries.hashCode() * 31

    override fun toString(): String = "SheetScene(key=$key, entry=$entry, previousEntries=$previousEntries)"
}
