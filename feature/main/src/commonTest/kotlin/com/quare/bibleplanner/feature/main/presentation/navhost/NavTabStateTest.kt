package com.quare.bibleplanner.feature.main.presentation.navhost

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class NavTabStateTest {
    private val tabs = listOf(
        MainNavRouteDestination.Plans,
        MainNavRouteDestination.Books,
        MainNavRouteDestination.Profile,
    )
    private lateinit var state: NavTabState
    private lateinit var backStacks: Map<MainNavRouteDestination, NavBackStack<NavKey>>

    @BeforeTest
    fun setUp() {
        backStacks = tabs.associateWith { tab -> NavBackStack<NavKey>(tab) }
        state = NavTabState(
            tabs = tabs,
            backStacks = backStacks,
            selectedIndexState = mutableIntStateOf(0),
        )
    }

    @Test
    fun `GIVEN the initial state WHEN reading it THEN the start tab is selected with nothing to go forward to`() {
        // When
        val selectedTab = state.selectedTab

        // Then
        assertEquals(MainNavRouteDestination.Plans, selectedTab)
        assertFalse(state.canGoForward)
    }

    @Test
    fun `GIVEN the start tab WHEN switching to another tab THEN selects it`() {
        // When
        state.switchTo(MainNavRouteDestination.Books)

        // Then
        assertEquals(MainNavRouteDestination.Books, state.selectedTab)
    }

    @Test
    fun `GIVEN a tab with a pushed route WHEN going back THEN pops the route and allows going forward`() {
        // Given
        state.switchTo(MainNavRouteDestination.Books)
        backStacks.getValue(MainNavRouteDestination.Books).add(ThemeNavRoute)

        // When
        state.goBack()

        // Then
        assertEquals(listOf<NavKey>(MainNavRouteDestination.Books), backStacks.getValue(MainNavRouteDestination.Books))
        assertEquals(MainNavRouteDestination.Books, state.selectedTab)
        assertTrue(state.canGoForward)
    }

    @Test
    fun `GIVEN a popped route WHEN going forward THEN pushes it back onto its tab`() {
        // Given
        backStacks.getValue(MainNavRouteDestination.Plans).add(LogoutNavRoute)
        state.goBack()

        // When
        state.goForward()

        // Then
        assertEquals(
            listOf<NavKey>(MainNavRouteDestination.Plans, LogoutNavRoute),
            backStacks.getValue(MainNavRouteDestination.Plans),
        )
        assertFalse(state.canGoForward)
    }

    @Test
    fun `GIVEN another tab at its root WHEN going back THEN returns to the start tab`() {
        // Given
        state.switchTo(MainNavRouteDestination.Profile)

        // When
        state.goBack()

        // Then
        assertEquals(MainNavRouteDestination.Plans, state.selectedTab)
        assertTrue(state.canGoForward)
    }

    @Test
    fun `GIVEN a tab left by going back WHEN going forward THEN reselects that tab`() {
        // Given
        state.switchTo(MainNavRouteDestination.Profile)
        state.goBack()

        // When
        state.goForward()

        // Then
        assertEquals(MainNavRouteDestination.Profile, state.selectedTab)
        assertFalse(state.canGoForward)
    }

    @Test
    fun `GIVEN the start tab at its root WHEN going back THEN nothing changes`() {
        // When
        state.goBack()

        // Then
        assertEquals(MainNavRouteDestination.Plans, state.selectedTab)
        assertFalse(state.canGoForward)
    }

    @Test
    fun `GIVEN nothing to go forward to WHEN going forward THEN nothing changes`() {
        // When
        state.goForward()

        // Then
        assertEquals(MainNavRouteDestination.Plans, state.selectedTab)
        assertEquals(listOf<NavKey>(MainNavRouteDestination.Plans), backStacks.getValue(MainNavRouteDestination.Plans))
    }

    @Test
    fun `GIVEN a forward history WHEN switching tabs THEN clears it`() {
        // Given
        state.switchTo(MainNavRouteDestination.Books)
        state.goBack()

        // When
        state.switchTo(MainNavRouteDestination.Profile)

        // Then
        assertFalse(state.canGoForward)
    }
}
