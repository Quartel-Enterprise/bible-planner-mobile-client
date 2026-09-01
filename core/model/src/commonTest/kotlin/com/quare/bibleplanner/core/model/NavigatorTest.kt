package com.quare.bibleplanner.core.model

import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NavigatorTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()

    @Test
    fun `GIVEN a collector WHEN navigating to a route THEN emits a navigate command`() = runTest {
        // Given
        prepareScenario()

        // When
        navigator.navigate(ThemeNavRoute)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.Navigate(ThemeNavRoute)), commands)
    }

    @Test
    fun `GIVEN a collector WHEN navigating replacing the top THEN emits a replacing top command`() = runTest {
        // Given
        prepareScenario()

        // When
        navigator.navigateReplacingTop(ThemeNavRoute)

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateReplacingTop(ThemeNavRoute)), commands)
    }

    @Test
    fun `GIVEN a collector WHEN navigating back THEN emits a back command`() = runTest {
        // Given
        prepareScenario()

        // When
        navigator.navigateBack()

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `GIVEN a collector WHEN sending commands in a row THEN delivers every one of them in order`() = runTest {
        // Given
        prepareScenario()

        // When
        navigator.navigate(ThemeNavRoute)
        navigator.navigate(LogoutNavRoute)
        navigator.navigateBack()

        // Then
        assertEquals(
            listOf<NavigationCommand>(
                NavigationCommand.Navigate(ThemeNavRoute),
                NavigationCommand.Navigate(LogoutNavRoute),
                NavigationCommand.NavigateBack,
            ),
            commands,
        )
    }

    @Test
    fun `GIVEN commands sent while nobody collects WHEN the collection starts THEN delivers the buffered ones`() =
        runTest {
            // Given
            navigator.navigate(ThemeNavRoute)
            navigator.navigate(LogoutNavRoute)

            // When
            prepareScenario()

            // Then
            assertEquals(
                listOf<NavigationCommand>(
                    NavigationCommand.Navigate(ThemeNavRoute),
                    NavigationCommand.Navigate(LogoutNavRoute),
                ),
                commands,
            )
        }

    private fun TestScope.prepareScenario() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect { commands += it }
        }
    }
}
