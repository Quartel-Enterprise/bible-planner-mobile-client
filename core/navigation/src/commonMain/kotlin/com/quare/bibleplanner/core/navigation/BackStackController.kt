package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.navigation.utils.hasStudyCompanionOnTop
import com.quare.bibleplanner.core.navigation.utils.popBackEntries

internal class BackStackController(
    private val backStack: MutableList<NavKey>,
    private val forwardStack: MutableList<List<NavKey>>,
) {
    val canNavigateForward: Boolean
        get() = forwardStack.isNotEmpty()

    fun navigate(route: NavKey) {
        if (route is ChapterStudyNavRoute && route.isCompanion) {
            showChapterStudyCompanion(route)
        } else if (route !in backStack) {
            backStack.add(route)
            forwardStack.clear()
        }
    }

    fun navigateReplacingTop(
        route: NavKey,
        isWide: Boolean,
    ) {
        if (route != backStack.lastOrNull()) {
            // The study beside the top entry belongs to it, so it goes too instead of being replaced.
            if (isWide && backStack.hasStudyCompanionOnTop()) {
                backStack.removeLastOrNull()
            }
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
     * The reader asks for the study of whichever chapter is on screen, so the one beside it follows
     * the text. Anything else on top, such as the verse selection, keeps its place: the study it
     * covers is replaced the next time the reader asks.
     */
    private fun showChapterStudyCompanion(route: ChapterStudyNavRoute) {
        when (backStack.lastOrNull()) {
            route -> Unit

            is ChapterStudyNavRoute -> if (backStack.hasStudyCompanionOnTop()) {
                backStack.removeLastOrNull()
                backStack.add(route)
            }

            is ReadNavRoute -> backStack.add(route)

            else -> Unit
        }
    }
}
