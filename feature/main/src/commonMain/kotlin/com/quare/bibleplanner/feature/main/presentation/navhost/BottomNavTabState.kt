package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.quare.bibleplanner.core.model.route.BottomNavRoute
import com.quare.bibleplanner.core.model.route.nav3SavedStateConfiguration

@Composable
internal fun rememberBottomNavTabState(): BottomNavTabState {
    val tabs: List<BottomNavRoute> = listOf(
        BottomNavRoute.Plans,
        BottomNavRoute.Books,
        BottomNavRoute.More,
    )
    val backStacks: Map<BottomNavRoute, NavBackStack<NavKey>> = tabs.associateWith { tab ->
        rememberNavBackStack(nav3SavedStateConfiguration, tab)
    }
    val selectedIndexState = rememberSaveable { mutableIntStateOf(0) }
    return remember {
        BottomNavTabState(
            tabs = tabs,
            backStacks = backStacks,
            selectedIndexState = selectedIndexState,
        )
    }
}

internal class BottomNavTabState(
    private val tabs: List<BottomNavRoute>,
    private val backStacks: Map<BottomNavRoute, NavBackStack<NavKey>>,
    selectedIndexState: MutableState<Int>,
) {
    private var selectedIndex by selectedIndexState
    private val startTab = tabs.first()
    val selectedTab: BottomNavRoute get() = tabs[selectedIndex]

    fun switchTo(tab: BottomNavRoute) {
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
