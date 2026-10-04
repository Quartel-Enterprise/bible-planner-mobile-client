package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ChapterStudyNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.toChapterStudyCompanion
import com.quare.bibleplanner.core.navigation.utils.hasStudyCompanionOnTop
import com.quare.bibleplanner.core.navigation.utils.popBackEntries
import com.quare.bibleplanner.core.navigation.utils.removeTopScreen

class BackStackController(
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
            backStack.removeTopScreen(isWide)
            backStack.add(route)
            forwardStack.clear()
        }
    }

    fun navigateReplacing(
        current: NavKey,
        route: NavKey,
    ) {
        val index = backStack.lastIndexOf(current)
        if (index < 0 || route in backStack) return
        backStack[index] = route
        replaceBelongingEntries(
            index = index,
            route = route,
        )
        forwardStack.clear()
    }

    /*
     * Why: what belongs to a screen leaves with it, as with navigateReplacingTop: the verse selection
     * over a replaced reader is dropped, and the study beside it follows the new chapter.
     */
    private fun replaceBelongingEntries(
        index: Int,
        route: NavKey,
    ) {
        val selectionIndex = backStack.indexOf(VerseSelectionNavRoute)
        if (selectionIndex > index) backStack.removeAt(selectionIndex)
        val companionIndex = index + 1
        val companion = backStack.getOrNull(companionIndex) as? ChapterStudyNavRoute
        if (route is ReadNavRoute && companion?.isCompanion == true) {
            backStack[companionIndex] = route.toChapterStudyCompanion()
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

    /*
     * Why: the study beside the reader follows the chapter on screen; anything else on top (e.g. verse
     * selection) keeps its place and the study it covers is replaced on the next reader request.
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
