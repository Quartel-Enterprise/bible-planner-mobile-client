package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.navigation.utils.popBackEntries

internal class BackStackController(
    private val backStack: MutableList<NavKey>,
    private val forwardStack: MutableList<List<NavKey>>,
) {
    val canNavigateForward: Boolean
        get() = forwardStack.isNotEmpty()

    fun navigate(route: NavKey) {
        if (route !in backStack) {
            if (isSwappingChapterStudy(route)) {
                backStack.removeLastOrNull()
            }
            backStack.add(route)
            forwardStack.clear()
        }
    }

    fun navigateReplacingTop(route: NavKey) {
        if (route != backStack.lastOrNull()) {
            backStack.removeLastOrNull()
            backStack.add(route)
            forwardStack.clear()
        }
    }

    fun navigateBack(isWide: Boolean) {
        val removed = backStack.popBackEntries(isWide = isWide)
        if (removed.isNotEmpty()) {
            forwardStack.add(removed)
        }
    }

    fun navigateForward() {
        forwardStack.removeLastOrNull()?.asReversed()?.forEach(backStack::add)
    }

    /**
     * On a wide window the reader stays usable beside an open chapter study, so the study of another
     * chapter can be asked for from there. It takes the open study's pane instead of stacking on it.
     */
    private fun isSwappingChapterStudy(route: NavKey): Boolean =
        route is ChapterStudyNavRoute && backStack.lastOrNull() is ChapterStudyNavRoute
}
