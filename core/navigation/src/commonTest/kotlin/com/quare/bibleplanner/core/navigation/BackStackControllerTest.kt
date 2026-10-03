package com.quare.bibleplanner.core.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.model.route.toChapterStudyCompanion
import com.quare.bibleplanner.core.model.route.toDayStudyNavRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BackStackControllerTest {
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
    private val genesisReadRoute = ReadNavRoute(
        bookId = "GEN",
        chapterNumber = 1,
        isChapterRead = false,
        isFromBookDetails = false,
        targetVerseNumbers = emptyList(),
    )
    private val exodusReadRoute = genesisReadRoute.copy(bookId = "EXO")
    private val genesisCompanionRoute: NavKey = genesisReadRoute.toChapterStudyCompanion()
    private val genesisTwoCompanionRoute: NavKey = genesisReadRoute.copy(chapterNumber = 2).toChapterStudyCompanion()

    private lateinit var backStackController: BackStackController
    private lateinit var backStack: MutableList<NavKey>
    private lateinit var forwardStack: MutableList<List<NavKey>>

    @Test
    fun `GIVEN a route absent from the back stack WHEN navigating THEN pushes it on top`() {
        // Given
        prepareScenario()

        // When
        backStackController.navigate(themeRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
    }

    @Test
    fun `GIVEN a route already in the back stack WHEN navigating THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute, logoutRoute))

        // When
        backStackController.navigate(themeRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute, logoutRoute), backStack)
    }

    @Test
    fun `GIVEN the reader on top WHEN navigating to its study companion THEN shows it beside the reader`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, genesisReadRoute))

        // When
        backStackController.navigate(genesisCompanionRoute)

        // Then
        assertEquals(listOf(mainRoute, genesisReadRoute, genesisCompanionRoute), backStack)
    }

    @Test
    fun `GIVEN a study companion on top WHEN navigating to the companion of another chapter THEN takes its place`() {
        // Given
        prepareScenario(
            backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute),
            forwardStack = listOf(listOf(themeRoute)),
        )

        // When
        backStackController.navigate(genesisTwoCompanionRoute)

        // Then
        assertEquals(listOf(mainRoute, genesisReadRoute, genesisTwoCompanionRoute), backStack)
        assertEquals(listOf(listOf(themeRoute)), forwardStack)
    }

    @Test
    fun `GIVEN the same study companion on top WHEN navigating to it THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute))

        // When
        backStackController.navigate(genesisCompanionRoute)

        // Then
        assertEquals(listOf(mainRoute, genesisReadRoute, genesisCompanionRoute), backStack)
    }

    @Test
    fun `GIVEN a panel over the study companion WHEN navigating to another companion THEN keeps it under the panel`() {
        // Given
        prepareScenario(
            backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute, VerseSelectionNavRoute),
        )

        // When
        backStackController.navigate(genesisTwoCompanionRoute)

        // Then
        assertEquals(
            listOf(mainRoute, genesisReadRoute, genesisCompanionRoute, VerseSelectionNavRoute),
            backStack,
        )
    }

    @Test
    fun `GIVEN no reader on top WHEN navigating to a study companion THEN keeps the back stack untouched`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))

        // When
        backStackController.navigate(genesisCompanionRoute)

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
    }

    @Test
    fun `GIVEN pending forward entries WHEN navigating THEN drops them`() {
        // Given
        prepareScenario(forwardStack = listOf(listOf(themeRoute)))

        // When
        backStackController.navigate(logoutRoute)

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
        backStackController.navigate(logoutRoute)

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
        backStackController.navigateReplacingTop(
            route = releaseNotesRoute,
            isWide = false,
        )

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
        backStackController.navigateReplacingTop(
            route = themeRoute,
            isWide = false,
        )

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
        assertEquals(listOf(listOf(logoutRoute)), forwardStack)
    }

    @Test
    fun `GIVEN the reader with its study beside in a wide layout WHEN replacing the top THEN replaces both panes`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute))

        // When
        backStackController.navigateReplacingTop(
            route = exodusReadRoute,
            isWide = true,
        )

        // Then
        assertEquals(listOf(mainRoute, exodusReadRoute), backStack)
    }

    @Test
    fun `GIVEN a study over the reader in a compact layout WHEN replacing the top THEN replaces only the study`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute))

        // When
        backStackController.navigateReplacingTop(
            route = themeRoute,
            isWide = false,
        )

        // Then
        assertEquals(listOf(mainRoute, genesisReadRoute, themeRoute), backStack)
    }

    @Test
    fun `GIVEN the reader with its study beside in a wide layout WHEN navigating back THEN pops both panes at once`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, genesisReadRoute, genesisCompanionRoute))

        // When
        backStackController.navigateBack(isWide = true)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(listOf(listOf(genesisCompanionRoute, genesisReadRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a stacked route WHEN navigating back THEN pops it into the forward stack`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))

        // When
        backStackController.navigateBack(isWide = false)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(listOf(listOf(themeRoute)), forwardStack)
    }

    @Test
    fun `GIVEN only the root entry WHEN navigating back THEN keeps the root and records nothing`() {
        // Given
        prepareScenario()

        // When
        backStackController.navigateBack(isWide = false)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN a day study companion on top in a wide layout WHEN navigating back THEN pops both panes at once`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))

        // When
        backStackController.navigateBack(isWide = true)

        // Then
        assertEquals(listOf(mainRoute), backStack)
        assertEquals(listOf(listOf(dayStudyRoute, dayRoute)), forwardStack)
    }

    @Test
    fun `GIVEN a day study companion on top in a compact layout WHEN navigating back THEN pops only the companion`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))

        // When
        backStackController.navigateBack(isWide = false)

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
        backStackController.navigateBack(isWide = true)

        // Then
        assertEquals(listOf(mainRoute, dayRoute), backStack)
    }

    @Test
    fun `GIVEN a popped pair of panes WHEN navigating forward THEN restores them in their original order`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, dayRoute, dayStudyRoute))
        backStackController.navigateBack(isWide = true)

        // When
        backStackController.navigateForward()

        // Then
        assertEquals(listOf(mainRoute, dayRoute, dayStudyRoute), backStack)
        assertEquals(emptyList(), forwardStack)
    }

    @Test
    fun `GIVEN several popped entries WHEN navigating forward THEN restores only the last popped one`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute, logoutRoute))
        backStackController.navigateBack(isWide = false)
        backStackController.navigateBack(isWide = false)

        // When
        backStackController.navigateForward()

        // Then
        assertEquals(listOf(mainRoute, themeRoute), backStack)
        assertEquals(listOf(listOf(logoutRoute)), forwardStack)
    }

    @Test
    fun `GIVEN no popped entry WHEN navigating forward THEN keeps the back stack untouched`() {
        // Given
        prepareScenario()

        // When
        backStackController.navigateForward()

        // Then
        assertEquals(listOf(mainRoute), backStack)
    }

    @Test
    fun `GIVEN no popped entry WHEN checking the forward availability THEN reports it as unavailable`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))

        // When
        val canNavigateForward = backStackController.canNavigateForward

        // Then
        assertFalse(canNavigateForward)
    }

    @Test
    fun `GIVEN a popped entry WHEN checking the forward availability THEN reports it as available`() {
        // Given
        prepareScenario(backStack = listOf(mainRoute, themeRoute))
        backStackController.navigateBack(isWide = false)

        // When
        val canNavigateForward = backStackController.canNavigateForward

        // Then
        assertTrue(canNavigateForward)
    }

    private fun prepareScenario(
        backStack: List<NavKey> = listOf(mainRoute),
        forwardStack: List<List<NavKey>> = emptyList(),
    ) {
        this.backStack = backStack.toMutableList()
        this.forwardStack = forwardStack.toMutableList()
        backStackController = BackStackController(
            backStack = this.backStack,
            forwardStack = this.forwardStack,
        )
    }

    companion object {
        private const val READING_PLAN_TYPE = "chronological"
    }
}
