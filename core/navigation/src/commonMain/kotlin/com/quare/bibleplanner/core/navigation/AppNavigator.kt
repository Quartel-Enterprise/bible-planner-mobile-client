package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.navigation.utils.popBackEntries

internal class AppNavigator(
    private val backStack: MutableList<NavKey>,
    private val forwardStack: MutableList<List<NavKey>>,
) {
    val canNavigateForward: Boolean
        get() = forwardStack.isNotEmpty()

    fun navigate(route: NavKey) {
        if (route !in backStack) {
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
}
