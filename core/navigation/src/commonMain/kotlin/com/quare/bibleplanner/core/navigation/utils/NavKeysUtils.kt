package com.quare.bibleplanner.core.navigation.utils

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute

internal fun MutableList<NavKey>.popBackEntries(isWide: Boolean): List<NavKey> {
    val removed = mutableListOf<NavKey>()
    if (isWide && hasStudyCompanionOnTop() && canPopEntry) {
        removeLastOrNull()?.let(removed::add)
    }
    if (canPopEntry) {
        removeLastOrNull()?.let(removed::add)
    }
    return removed
}

// Why: the selection panel and the study beside a screen belong to it, so replacing the screen
// removes them too; leaving the panel would replace it instead and keep the old screen under the new one.
internal fun MutableList<NavKey>.removeTopScreen(isWide: Boolean) {
    if (lastOrNull() == VerseSelectionNavRoute) {
        removeLastOrNull()
    }
    if (isWide && hasStudyCompanionOnTop()) {
        removeLastOrNull()
    }
    removeLastOrNull()
}

private val List<NavKey>.canPopEntry: Boolean
    get() = size > 1
