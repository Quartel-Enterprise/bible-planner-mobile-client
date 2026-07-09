package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination

internal class NavTabState(
    private val tabs: List<MainNavRouteDestination>,
    private val backStacks: Map<MainNavRouteDestination, NavBackStack<NavKey>>,
    selectedIndexState: MutableState<Int>,
) {
    private var selectedIndex by selectedIndexState
    private val startTab = tabs.first()
    val selectedTab: MainNavRouteDestination get() = tabs[selectedIndex]

    fun switchTo(tab: MainNavRouteDestination) {
        selectedIndex = tabs.indexOf(tab)
    }

    fun goBack() {
        val currentStack = backStacks.getValue(selectedTab)
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (selectedTab != startTab) {
            selectedIndex = tabs.indexOf(startTab)
        }
    }

    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider = entryProvider,
            )
        }
        val tabsInUse = if (selectedTab == startTab) {
            listOf(startTab)
        } else {
            listOf(startTab, selectedTab)
        }
        return tabsInUse.flatMap { decoratedEntries.getValue(it) }
    }
}
