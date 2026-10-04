package com.quare.bibleplanner.tools.agentcli.session

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.AppViewModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.navigation.BackStackController
import com.quare.bibleplanner.core.navigation.utils.hasStudyCompanionOnTop
import com.quare.bibleplanner.core.navigation.utils.syncStudyPanelCompanion
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackDestination
import com.quare.bibleplanner.tools.agentcli.catalog.RouteCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ScreenCatalog
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/*
 * Why: everything here runs on Dispatchers.Main, like the composition that owns the back stack in
 * the app, so the ViewModels see the same threading they see on a device.
 */
class AgentSession(
    private val routeCatalog: RouteCatalog,
    private val screenCatalog: ScreenCatalog,
    private val entryFactory: EntryFactory,
    private val navigator: Navigator,
    private val snackbarController: AppSnackbarController,
    private val trackDestination: TrackDestination,
    private val log: SessionLog,
    private val encoder: StateEncoder,
    private val timeSource: TimeSource,
    private val isWide: Boolean,
) {
    private val pollInterval = 20.milliseconds
    private val backStack = mutableListOf<NavKey>()
    private val forwardStack = mutableListOf<List<NavKey>>()
    private val backStackController = BackStackController(
        backStack = backStack,
        forwardStack = forwardStack,
    )
    private val entries = LinkedHashMap<NavKey, HeadlessEntry>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var shownRoute: NavKey? = null
    lateinit var appEntry: HeadlessEntry
        private set

    val stack: List<NavKey>
        get() = backStack.toList()

    val topEntry: HeadlessEntry
        get() = entries.getValue(backStack.last())

    // Why: on a wide window the study companion shares the screen with the day or chapter under it.
    val visibleViewModels: List<HeadlessViewModel>
        get() {
            val shown = if (backStack.hasStudyCompanionOnTop()) backStack.takeLast(2) else backStack.takeLast(1)
            return shown.flatMap { route -> entries.getValue(route).viewModels }
        }

    fun start(initialRoute: NavKey) {
        appEntry = entryFactory.create(
            route = null,
            parameter = null,
            viewModelClasses = screenCatalog.appViewModels,
        )
        appEntry.viewModels
            .map(HeadlessViewModel::viewModel)
            .filterIsInstance<AppViewModel>()
            .forEach(AppViewModel::onAppForegrounded)
        updateStack { backStack += initialRoute }
        scope.launch {
            navigator.commands.collect { command ->
                runCatching { execute(command) }.onFailure { error ->
                    log.record(
                        kind = LogKind.LOG,
                        source = "Navigator",
                        payload = buildJsonObject {
                            put("severity", JsonPrimitive("Error"))
                            put("message", JsonPrimitive("could not show ${command::class.simpleName}"))
                            put("error", JsonPrimitive(error.toString()))
                        },
                    )
                }
            }
        }
        scope.launch {
            snackbarController.messages.collect { message ->
                log.record(
                    kind = LogKind.SNACKBAR,
                    source = "AppSnackbarController",
                    payload = encoder.encode(
                        value = message,
                        maxItems = 0,
                    ),
                )
            }
        }
    }

    fun reset(initialRoute: NavKey) {
        updateStack {
            backStack.clear()
            forwardStack.clear()
            backStack += initialRoute
        }
    }

    suspend fun awaitSettled(
        quiet: Duration,
        timeout: Duration,
    ): Boolean {
        val start = timeSource.markNow()
        while (start.elapsedNow() < timeout) {
            delay(pollInterval)
            if (log.lastActivity.elapsedNow() >= quiet) return true
        }
        return false
    }

    fun execute(command: NavigationCommand) {
        updateStack {
            when (command) {
                is NavigationCommand.Navigate -> backStackController.navigate(command.route)

                is NavigationCommand.NavigateReplacingTop -> backStackController.navigateReplacingTop(
                    route = command.route,
                    isWide = isWide,
                )

                is NavigationCommand.NavigateReplacing -> backStackController.navigateReplacing(
                    current = command.current,
                    route = command.route,
                )

                NavigationCommand.NavigateBack -> backStackController.navigateBack(isWide)
            }
        }
        log.record(
            kind = LogKind.NAVIGATION,
            source = "Navigator",
            payload = buildJsonObject {
                put("command", encoder.encode(value = command, maxItems = 0))
                put("stack", JsonPrimitive(backStack.joinToString(separator = " > ", transform = routeCatalog::nameOf)))
            },
        )
    }

    /*
     * Why: a screen opens only once all its ViewModels were built. When one fails, the stack and every
     * open screen stay as they were, and the error answers the command.
     */
    private fun updateStack(change: () -> Unit) {
        val previousBackStack = backStack.toList()
        val previousForwardStack = forwardStack.toList()
        change()
        // Why: on a wide window the app opens the study beside the day or the chapter on screen.
        backStack.syncStudyPanelCompanion(
            isWide = isWide,
            isCollapsingCompanion = false,
        )
        runCatching(::createMissingEntries).onFailure { error ->
            backStack.clear()
            backStack += previousBackStack
            forwardStack.clear()
            forwardStack += previousForwardStack
            throw error
        }
        entries.keys
            .filter { route -> route !in backStack }
            .forEach { route -> entries.remove(route)?.close() }
        val top = backStack.lastOrNull()
        if (top != null && top != shownRoute) {
            shownRoute = top
            trackDestination(top)
        }
    }

    private fun createMissingEntries() {
        val created = LinkedHashMap<NavKey, HeadlessEntry>()
        runCatching {
            backStack.filter { route -> route !in entries }.forEach { route ->
                created[route] = entryFactory
                    .create(
                        route = route,
                        parameter = screenCatalog.parameterFor(route),
                        viewModelClasses = screenCatalog.viewModelsFor(route::class),
                    ).also { entry -> entry.viewModels.forEach { viewModel -> viewModel.reportWidthClass(isWide) } }
            }
        }.onFailure { error ->
            created.values.forEach(HeadlessEntry::close)
            throw error
        }
        entries += created
    }
}
