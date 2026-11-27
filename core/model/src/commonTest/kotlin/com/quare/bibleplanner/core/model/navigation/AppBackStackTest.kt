package com.quare.bibleplanner.core.model.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AppBackStackTest {
    private val mainRoute: NavKey = MainNavRoute
    private val themeRoute: NavKey = ThemeNavRoute
    private val logoutRoute: NavKey = LogoutNavRoute

    @Test
    fun `GIVEN a new back stack WHEN reading entries THEN holds only the initial route`() {
        // Given
        val appBackStack = AppBackStack()

        // When
        val entries = appBackStack.entries.value

        // Then
        assertEquals(listOf(mainRoute), entries)
        assertFalse(appBackStack.canNavigateForward.value)
        assertFalse(appBackStack.hasStudyCompanionOnTop)
    }

    @Test
    fun `GIVEN navigate commands WHEN following them THEN publishes the pushed routes`() = runTest {
        // Given
        val appBackStack = AppBackStack()

        // When
        appBackStack.follow(
            flowOf(
                NavigationCommand.Navigate(themeRoute),
                NavigationCommand.NavigateReplacingTop(logoutRoute),
            ),
        )

        // Then
        assertEquals(listOf(mainRoute, logoutRoute), appBackStack.entries.value)
    }

    @Test
    fun `GIVEN a pushed route WHEN navigating back THEN pops it and enables forward`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(themeRoute))

        // When
        appBackStack.execute(NavigationCommand.NavigateBack)

        // Then
        assertEquals(listOf(mainRoute), appBackStack.entries.value)
        assertTrue(appBackStack.canNavigateForward.value)
    }

    @Test
    fun `GIVEN a popped route WHEN navigating forward THEN pushes it again and disables forward`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(themeRoute))
        appBackStack.navigateBack()

        // When
        appBackStack.navigateForward()

        // Then
        assertEquals(listOf(mainRoute, themeRoute), appBackStack.entries.value)
        assertFalse(appBackStack.canNavigateForward.value)
    }

    @Test
    fun `GIVEN two routes WHEN replacing one THEN swaps it in place`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(themeRoute))

        // When
        appBackStack.execute(
            NavigationCommand.NavigateReplacing(
                current = themeRoute,
                route = logoutRoute,
            ),
        )

        // Then
        assertEquals(listOf(mainRoute, logoutRoute), appBackStack.entries.value)
    }

    @Test
    fun `GIVEN saved routes WHEN restoring THEN replaces the stack and clears the forward history`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(logoutRoute))
        appBackStack.navigateBack()

        // When
        appBackStack.restore(listOf(mainRoute, themeRoute))

        // Then
        assertEquals(listOf(mainRoute, themeRoute), appBackStack.entries.value)
        assertFalse(appBackStack.canNavigateForward.value)
    }

    @Test
    fun `GIVEN an empty list WHEN restoring THEN keeps the current stack`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(themeRoute))

        // When
        appBackStack.restore(emptyList())

        // Then
        assertEquals(listOf(mainRoute, themeRoute), appBackStack.entries.value)
    }

    @Test
    fun `GIVEN a deep stack WHEN resetting THEN goes back to the initial route`() {
        // Given
        val appBackStack = AppBackStack()
        appBackStack.execute(NavigationCommand.Navigate(themeRoute))

        // When
        appBackStack.reset()

        // Then
        assertEquals(listOf(mainRoute), appBackStack.entries.value)
    }

    @Test
    fun `GIVEN a wide layout WHEN updating the layout THEN stores the width`() {
        // Given
        val appBackStack = AppBackStack()

        // When
        appBackStack.updateLayout(
            isWide = true,
            isCollapsingCompanion = false,
        )

        // Then
        assertTrue(appBackStack.isWide)
        assertEquals(listOf(mainRoute), appBackStack.entries.value)
    }
}
