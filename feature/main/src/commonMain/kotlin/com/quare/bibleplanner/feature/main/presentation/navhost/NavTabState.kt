package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
    private val forwardStack = mutableStateListOf<TabBackStep>()
    val selectedTab: MainNavRouteDestination get() = tabs[selectedIndex]
    val canGoForward: Boolean get() = forwardStack.isNotEmpty()

    fun switchTo(tab: MainNavRouteDestination) {
        selectedIndex = tabs.indexOf(tab)
        forwardStack.clear()
    }

    fun goBack() {
        val currentStack = backStacks.getValue(selectedTab)
        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()?.let { route ->
                forwardStack.add(TabBackStep(tab = selectedTab, poppedRoute = route))
            }
        } else if (selectedTab != startTab) {
            forwardStack.add(TabBackStep(tab = selectedTab, poppedRoute = null))
            selectedIndex = tabs.indexOf(startTab)
        }
    }

    fun goForward() {
        val step = forwardStack.removeLastOrNull() ?: return
        if (step.poppedRoute != null) {
            backStacks.getValue(step.tab).add(step.poppedRoute)
        } else {
            selectedIndex = tabs.indexOf(step.tab)
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
