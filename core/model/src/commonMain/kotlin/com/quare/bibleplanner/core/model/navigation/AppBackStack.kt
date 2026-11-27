package com.quare.bibleplanner.core.model.navigation

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.route.MainNavRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/*
 * Why: the back stack lives outside any UI toolkit so Compose, SwiftUI and React draw the same
 * entries; each UI only reports its width and the back or forward gestures it receives.
 */
class AppBackStack(
    initialRoute: NavKey = MainNavRoute,
) {
    private val backStack: MutableList<NavKey> = mutableListOf(initialRoute)
    private val forwardStack: MutableList<List<NavKey>> = mutableListOf()
    private val controller = BackStackController(
        backStack = backStack,
        forwardStack = forwardStack,
    )

    val entries: StateFlow<List<NavKey>>
        field = MutableStateFlow(backStack.toList())

    val canNavigateForward: StateFlow<Boolean>
        field = MutableStateFlow(false)

    var isWide: Boolean = false
        private set

    val hasStudyCompanionOnTop: Boolean
        get() = backStack.hasStudyCompanionOnTop()

    /*
     * Why: the host UI follows the commands from its composition, on the main thread, after it restored
     * the saved routes; a command sent before the first frame (a notification tap in onCreate) then
     * lands on the restored stack instead of being wiped by the restore. Only one host per process
     * follows them, since every UI draws this same stack.
     */
    suspend fun follow(commands: Flow<NavigationCommand>) {
        commands.collect(::execute)
    }

    fun execute(command: NavigationCommand) {
        update {
            when (command) {
                is NavigationCommand.Navigate -> controller.navigate(command.route)

                is NavigationCommand.NavigateReplacingTop -> controller.navigateReplacingTop(
                    route = command.route,
                    isWide = isWide,
                )

                is NavigationCommand.NavigateReplacing -> controller.navigateReplacing(
                    current = command.current,
                    route = command.route,
                )

                NavigationCommand.NavigateBack -> controller.navigateBack(isWide)
            }
        }
    }

    fun navigateBack() {
        update { controller.navigateBack(isWide) }
    }

    fun navigateForward() {
        update { controller.navigateForward() }
    }

    fun updateLayout(
        isWide: Boolean,
        isCollapsingCompanion: Boolean,
    ) {
        update {
            this.isWide = isWide
            backStack.syncStudyPanelCompanion(
                isWide = isWide,
                isCollapsingCompanion = isCollapsingCompanion,
            )
        }
    }

    /*
     * Why: each new host starts from the routes it saved (or the initial route), so a new activity in a
     * process that is still alive doesn't reopen the previous session; the forward history is not saved,
     * so it starts empty.
     */
    fun restore(routes: List<NavKey>) {
        update {
            if (routes.isEmpty()) return
            backStack.clear()
            backStack.addAll(routes)
            forwardStack.clear()
        }
    }

    private inline fun update(block: () -> Unit) {
        block()
        entries.value = backStack.toList()
        canNavigateForward.value = controller.canNavigateForward
    }
}
