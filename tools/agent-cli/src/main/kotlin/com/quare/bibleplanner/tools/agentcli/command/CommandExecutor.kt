package com.quare.bibleplanner.tools.agentcli.command

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.tools.agentcli.catalog.RouteCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ScreenCatalog
import com.quare.bibleplanner.tools.agentcli.json.ArgumentDecoder
import com.quare.bibleplanner.tools.agentcli.json.JsonPath
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.session.AgentSession
import com.quare.bibleplanner.tools.agentcli.session.HeadlessViewModel
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class CommandExecutor(
    private val session: AgentSession,
    private val routeCatalog: RouteCatalog,
    private val screenCatalog: ScreenCatalog,
    private val decoder: ArgumentDecoder,
    private val encoder: StateEncoder,
    private val timeSource: TimeSource,
    private val initialRoute: NavKey,
) {
    private val waitInterval = 50.milliseconds
    private val help = listOf(
        "routes [filter]                      every route, its arguments and the ViewModels it shows",
        "open <Route> [{json}]                navigate to a route, e.g. open DayNavRoute {\"dayNumber\":1,...}",
        "replace <Route> [{json}]             navigate replacing the top screen",
        "back                                 navigate back",
        "reset                                go back to a fresh main screen",
        "stack                                the back stack with each route's arguments",
        "state [path] [--limit n] [--app]     state of the top screen (or the app-level ViewModels)",
        "events [ViewModel]                   the events the top screen's ViewModels accept",
        "event [ViewModel.]Name [{json}]      send an event, e.g. event OnChapterClicked {\"chapterNumber\":2}",
        "functions [ViewModel]                the public functions of the top screen's ViewModels",
        "call ViewModel.function [{json}]     call a public ViewModel function with named arguments",
        "wait <path>[ = json] [--timeout ms]  wait until a state path exists, or equals a value",
        "settle [ms]                          wait until nothing changed for ms (default 300)",
        "log                                  only the navigation, actions, analytics and logs since the last command",
        "quit                                 stop the agent CLI",
    )

    suspend fun execute(command: Command): JsonElement = when (command) {
        Command.Help -> JsonArray(help.map(::JsonPrimitive))

        is Command.Routes -> getRoutes(command.filter)

        is Command.Open -> open(command)

        Command.Back -> {
            session.execute(NavigationCommand.NavigateBack)
            getScreen()
        }

        Command.Reset -> {
            session.reset(initialRoute)
            getScreen()
        }

        Command.Stack -> getStack()

        is Command.State -> readState(
            isApp = command.isApp,
            maxItems = command.maxItems,
            path = command.path,
        )

        is Command.Events -> getEvents(command.viewModel)

        is Command.Event -> sendEvent(command)

        is Command.Functions -> getFunctions(command.viewModel)

        is Command.Call -> call(command)

        is Command.Settle -> JsonPrimitive(
            session.awaitSettled(
                quiet = command.quiet,
                timeout = command.quiet * SETTLE_TIMEOUT_FACTOR,
            ),
        )

        is Command.Wait -> awaitState(command)

        Command.Log -> JsonArray(emptyList())

        Command.Quit -> JsonPrimitive("bye")
    }

    private fun getRoutes(filter: String?): JsonElement = JsonArray(
        routeCatalog.routes
            .filter { route -> filter == null || route.name.contains(filter, ignoreCase = true) }
            .map { route ->
                val viewModels = screenCatalog.viewModelsFor(route.kClass).map { it.simpleName }
                val shown = if (viewModels.isEmpty()) "no ViewModel on the JVM" else viewModels.joinToString()
                JsonPrimitive("${route.name}${routeCatalog.getSignature(route)} -> $shown")
            },
    )

    private fun open(command: Command.Open): JsonElement {
        val descriptor = routeCatalog.find(command.route)
        val route = routeCatalog.create(
            route = descriptor,
            arguments = command.arguments,
        )
        session.execute(
            if (command.isReplacingTop) {
                NavigationCommand.NavigateReplacingTop(route)
            } else {
                NavigationCommand.Navigate(route)
            },
        )
        return getScreen()
    }

    fun getScreen(): JsonElement = buildJsonObject {
        put("screen", session.topEntry.route?.let(routeCatalog::nameOf))
        put("viewModels", JsonArray(session.visibleViewModels.map { JsonPrimitive(it.name) }))
        put("stack", session.stack.joinToString(separator = " > ", transform = routeCatalog::nameOf))
    }

    private fun getStack(): JsonElement = JsonArray(
        session.stack.map { route ->
            buildJsonObject {
                put("route", routeCatalog.nameOf(route))
                put(
                    "arguments",
                    encoder.encode(
                        value = route,
                        maxItems = 0,
                    ),
                )
            }
        },
    )

    private fun readState(
        isApp: Boolean,
        maxItems: Int,
        path: String?,
    ): JsonElement {
        val shownViewModels = if (isApp) session.appEntry.viewModels else session.visibleViewModels
        val viewModelName = path?.substringBefore('.')?.substringBefore('[')
        val propertyName = path
            ?.substringAfter('.', missingDelimiterValue = "")
            ?.substringBefore('.')
            ?.substringBefore('[')
            ?.ifEmpty { null }
        val viewModels = shownViewModels
            .filter { viewModel -> viewModel.name == viewModelName }
            .ifEmpty { shownViewModels }
        val state = JsonObject(
            viewModels.associate { viewModel ->
                viewModel.name to viewModel.readState(
                    maxItems = maxItems,
                    onlyProperty = propertyName,
                )
            },
        )
        return path?.let { JsonPath(it).select(state) } ?: state
    }

    private fun getEvents(viewModelName: String?): JsonElement = JsonObject(
        viewModelsFor(viewModelName).associate { viewModel ->
            viewModel.name to JsonArray(
                viewModel.eventTypes.map { eventType ->
                    val parameters = eventType.primaryConstructor?.parameters.orEmpty()
                    val signature = if (eventType.objectInstance != null) "" else decoder.getSignature(parameters)
                    JsonPrimitive(eventType.simpleName + signature)
                },
            )
        },
    )

    private fun getFunctions(viewModelName: String?): JsonElement = JsonObject(
        viewModelsFor(viewModelName).associate { viewModel ->
            viewModel.name to JsonArray(viewModel.getFunctions(decoder).map(::JsonPrimitive))
        },
    )

    private fun sendEvent(command: Command.Event): JsonElement {
        val viewModelName = command.target.substringBeforeLast('.', missingDelimiterValue = "").ifBlank { null }
        val eventName = command.target.substringAfterLast('.')
        val matches = viewModelsFor(viewModelName).flatMap { viewModel ->
            viewModel.eventTypes
                .filter { eventType -> eventType.simpleName == eventName }
                .map { eventType -> viewModel to eventType }
        }
        val (viewModel, eventType) = when (matches.size) {
            1 -> matches.single()

            0 -> throw IllegalArgumentException("no event $eventName on this screen; run `events`")

            else -> throw IllegalArgumentException(
                "$eventName exists on ${matches.map { it.first.name }}; use ViewModel.$eventName",
            )
        }
        viewModel.send(
            decoder.build(
                kClass = eventType,
                arguments = command.arguments,
            ),
        )
        return getScreen()
    }

    private suspend fun call(command: Command.Call): JsonElement {
        val viewModelName = command.target.substringBeforeLast('.', missingDelimiterValue = "")
        require(viewModelName.isNotBlank()) { "usage: call ViewModel.function [{json}]" }
        val viewModel = viewModelsFor(viewModelName).single()
        return viewModel.call(
            functionName = command.target.substringAfterLast('.'),
            arguments = command.arguments,
            decoder = decoder,
            maxItems = command.maxItems,
        )
    }

    private suspend fun awaitState(command: Command.Wait): JsonElement {
        val start = timeSource.markNow()
        var last: JsonElement? = null
        while (start.elapsedNow() < command.timeout) {
            last = runCatching {
                readState(
                    isApp = command.isApp,
                    maxItems = 0,
                    path = command.path,
                )
            }.getOrNull()
            val isMet = if (command.expected == null) last != null && last != JsonNull else last == command.expected
            if (isMet) return last ?: JsonNull
            delay(waitInterval)
        }
        throw IllegalStateException("timed out after ${command.timeout} waiting for ${command.path}; last value: $last")
    }

    private fun viewModelsFor(name: String?): List<HeadlessViewModel> {
        val available = session.visibleViewModels + session.appEntry.viewModels
        if (name == null) return session.visibleViewModels
        val matches = available.filter { viewModel -> viewModel.name.equals(name, ignoreCase = true) }
        require(matches.isNotEmpty()) { "no $name here; this screen has ${available.map(HeadlessViewModel::name)}" }
        return matches
    }

    private companion object {
        const val SETTLE_TIMEOUT_FACTOR = 20
    }
}
