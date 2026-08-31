package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AppNavigatorTest {
    private val mainRoute: NavKey = MainNavRoute
    private val themeRoute: NavKey = ThemeNavRoute
    private val logoutRoute: NavKey = LogoutNavRoute
    private val releaseNotesRoute: NavKey = ReleaseNotesNavRoute
    private val dayRoute = DayNavRoute(
        dayNumber = 1,
        weekNumber = 1,
        readingPlanType = READING_PLAN_TYPE,
    )
    private val dayStudyRoute: NavKey = dayRoute.toDayStudyNavRoute()

    private lateinit var navigator: AppNavigator
    private lateinit var backStack: MutableList<NavKey>
    private lateinit var forwardStack: MutableList<List<NavKey>>

    @Test
    fun `GIVEN a route absent from the back stack WHEN navigating THEN pushes it on top`() {
        // Given
        prepareScenario()

        // When
        navigator.navigate(themeRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
    }

    @Test
    fun `GIVEN a route already in the back stack WHEN navigating THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute, logoutRoute))

        // When
        navigator.navigate(themeRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute, logoutRoute), backStack)
    }

    @Test
    fun `GIVEN pending forward entries WHEN navigating THEN drops them`() {
        // Given
        prepareScenario(forwardStack = listOf(listOf(themeRoute)))

        // When
        navigator.navigate(logoutRoute)

        // Then
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN a route already in the back stack WHEN navigating THEN keeps the forward entries`() {
        // Given
        prepareScenario(
            backStack = listOf(mainRoute, logoutRoute),
            forwardStack = listOf(listOf(themeRoute)),
        )

        // When
        navigator.navigate(logoutRoute)

        // Then
        assertEquals(listOf(listOf(themeRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a route different from the top WHEN navigating replacing top THEN swaps the top entry`() {
        // Given
        prepareScenario(
            backStack = listOf(mainRoute, themeRoute),
            forwardStack = listOf(listOf(logoutRoute)),
        )

        // When
        navigator.navigateReplacingTop(releaseNotesRoute)

        // Then
        assertEquals(listOf(mainRoute, releaseNotesRoute), backStack)
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN the route is already the top WHEN navigating replacing top THEN keeps both stacks untouched`() {
        // Given
        prepareScenario(
            backStack = listOf(mainRoute, themeRoute),
            forwardStack = listOf(listOf(logoutRoute)),
        )

        // When
        navigator.navigateReplacingTop(themeRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
        assertEquals(listOf(listOf(logoutRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a stacked route WHEN navigating back THEN pops it into the forward stack`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))

        // When
        navigator.navigateBack(isWide = false)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(listOf(listOf(themeRoute)), forwardStack)
    }

    @Test
    fun `GIVEN only the root entry WHEN navigating back THEN keeps the root and records nothing`() {
        // Given
        prepareScenario()

        // When
        navigator.navigateBack(isWide = false)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN a day study companion on top in a wide layout WHEN navigating back THEN pops both panes at once`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))

        // When
        navigator.navigateBack(isWide = true)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(listOf(listOf(dayStudyRoute, dayRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a day study companion on top in a compact layout WHEN navigating back THEN pops only the companion`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))

        // When
        navigator.navigateBack(isWide = false)

        // Then
        assertEquals(listOf(mainRoute, dayRoute), backStack)
        assertEquals(listOf(listOf(dayStudyRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a study pane of another day on top in a wide layout WHEN navigating back THEN pops only the top`() {
        // Given
        prepareScenario(
            backStack = listOf(
                mainRoute,
                dayRoute,
                DayNavRoute(
                    dayNumber = 2,
                    weekNumber = 1,
                    readingPlanType = READING_PLAN_TYPE,
                ).toDayStudyNavRoute(),
            ),
        )

        // When
        navigator.navigateBack(isWide = true)

        // Then
        assertEquals(listOf(mainRoute, dayRoute), backStack)
    }

    @Test
    fun `GIVEN a popped pair of panes WHEN navigating forward THEN restores them in their original order`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))
        navigator.navigateBack(isWide = true)

        // When
        navigator.navigateForward()

        // Then
        assertEquals(listOf(mainRoute, dayRoute, dayStudyRoute), backStack)
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN several popped entries WHEN navigating forward THEN restores only the last popped one`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute, logoutRoute))
        navigator.navigateBack(isWide = false)
        navigator.navigateBack(isWide = false)

        // When
        navigator.navigateForward()

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
        assertEquals(listOf(listOf(logoutRoute)), forwardStack)
    }

    @Test
    fun `GIVEN no popped entry WHEN navigating forward THEN keeps the back stack untouched`() {
        // Given
        prepareScenario()

        // When
        navigator.navigateForward()

        // Then
        assertEquals(listOf(mainRoute), backStack)
    }

    @Test
    fun `GIVEN no popped entry WHEN checking the forward availability THEN reports it as unavailable`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))

        // When
        val canNavigateForward = navigator.canNavigateForward

        // Then
        assertFalse(canNavigateForward)
    }

    @Test
    fun `GIVEN a popped entry WHEN checking the forward availability THEN reports it as available`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))
        navigator.navigateBack(isWide = false)

        // When
        val canNavigateForward = navigator.canNavigateForward

        // Then
        assertTrue(canNavigateForward)
    }

    private fun prepareScenario(
        backStack: List<NavKey> = listOf(mainRoute),
        forwardStack: List<List<NavKey>> = emptyList(),
    ) {
        this.backStack = backStack.toMutableList()
        this.forwardStack = forwardStack.toMutableList()
        navigator = AppNavigator(
            backStack = this.backStack,
            forwardStack = this.forwardStack,
        )
    }

    companion object {
        private const val READING_PLAN_TYPE = "chronological"
    }
}
