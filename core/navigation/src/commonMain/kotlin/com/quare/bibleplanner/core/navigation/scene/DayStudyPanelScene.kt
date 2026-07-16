package com.quare.bibleplanner.core.navigation.scene

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val READING_WEIGHT = 2f
private const val STUDY_WEIGHT = 3f

internal class DayStudyPanelScene(
    override val key: Any,
    private val mainEntry: NavEntry<NavKey>,
    private val detailEntry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {
    override val entries: List<NavEntry<NavKey>> = listOf(mainEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(READING_WEIGHT)
                    .fillMaxHeight(),
            ) {
                mainEntry.Content()
            }
            VerticalDivider()
            Box(
                modifier = Modifier
                    .weight(STUDY_WEIGHT)
                    .fillMaxHeight(),
            ) {
                detailEntry.Content()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DayStudyPanelScene

        return key == other.key &&
            mainEntry == other.mainEntry &&
            detailEntry == other.detailEntry &&
            previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = key.hashCode() * 31 +
        mainEntry.hashCode() * 31 +
        detailEntry.hashCode() * 31 +
        previousEntries.hashCode() * 31

    override fun toString(): String = "DayStudyPanelScene(key=$key, mainEntry=$mainEntry, detailEntry=$detailEntry, " +
        "previousEntries=$previousEntries)"
}
