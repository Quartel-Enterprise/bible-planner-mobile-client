package com.quare.bibleplanner.tools.agentcli.command

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

internal class CommandParserTest {
    private val parser = CommandParser(Json)

    @Test
    fun `parses a route with its JSON arguments`() {
        // When
        val command = parser.parse("open DayNavRoute {\"dayNumber\":1,\"weekNumber\":2}")

        // Then
        assertEquals(
            expected = Command.Open(
                route = "DayNavRoute",
                arguments = JsonObject(mapOf("dayNumber" to JsonPrimitive(1), "weekNumber" to JsonPrimitive(2))),
                isReplacingTop = false,
            ),
            actual = command,
        )
    }

    @Test
    fun `replace opens the route over the top screen`() {
        // When
        val command = parser.parse("replace ThemeNavRoute")

        // Then
        assertEquals(
            expected = Command.Open(
                route = "ThemeNavRoute",
                arguments = JsonObject(emptyMap()),
                isReplacingTop = true,
            ),
            actual = command,
        )
    }

    @Test
    fun `parses a state path with its options`() {
        // When
        val command = parser.parse("state AppViewModel.themeState --limit 5 --app")

        // Then
        assertEquals(
            expected = Command.State(
                path = "AppViewModel.themeState",
                maxItems = 5,
                isApp = true,
            ),
            actual = command,
        )
    }

    @Test
    fun `state without options shows fifty items of the top screen`() {
        // When
        val command = parser.parse("STATE")

        // Then
        assertEquals(
            expected = Command.State(
                path = null,
                maxItems = 50,
                isApp = false,
            ),
            actual = command,
        )
    }

    @Test
    fun `parses an event aimed at one ViewModel`() {
        // When
        val command = parser.parse("event DayViewModel.OnNotesChanged {\"notes\":\"a {brace}\"}")

        // Then
        assertEquals(
            expected = Command.Event(
                target = "DayViewModel.OnNotesChanged",
                arguments = JsonObject(mapOf("notes" to JsonPrimitive("a {brace}"))),
            ),
            actual = command,
        )
    }

    @Test
    fun `parses a wait for a JSON value`() {
        // When
        val command = parser.parse("wait Vm.uiState.status = \"Done\" --timeout 2000 --app")

        // Then
        assertEquals(
            expected = Command.Wait(
                path = "Vm.uiState.status",
                expected = JsonPrimitive("Done"),
                timeout = 2000.milliseconds,
                isApp = true,
            ),
            actual = command,
        )
    }

    @Test
    fun `a wait value that is not JSON is read as text`() {
        // When
        val command = parser.parse("wait Vm.uiState.@type = Loaded")

        // Then
        assertEquals(
            expected = Command.Wait(
                path = "Vm.uiState.@type",
                expected = JsonPrimitive("Loaded"),
                timeout = 10_000.milliseconds,
                isApp = false,
            ),
            actual = command,
        )
    }

    @Test
    fun `a wait value may hold a double dash`() {
        // When
        val command = parser.parse("wait Vm.uiState.title = \"Day 1 -- Genesis\" --app")

        // Then
        assertEquals(
            expected = Command.Wait(
                path = "Vm.uiState.title",
                expected = JsonPrimitive("Day 1 -- Genesis"),
                timeout = 10_000.milliseconds,
                isApp = true,
            ),
            actual = command,
        )
    }

    @Test
    fun `a wait without a value waits for the path to exist`() {
        // When
        val command = parser.parse("wait Vm.uiState.content")

        // Then
        assertEquals(
            expected = Command.Wait(
                path = "Vm.uiState.content",
                expected = null,
                timeout = 10_000.milliseconds,
                isApp = false,
            ),
            actual = command,
        )
    }

    @Test
    fun `parses the commands without arguments`() {
        // When
        val commands = listOf(
            "help",
            "?",
            "back",
            "reset",
            "stack",
            "log",
            "quit",
            "exit",
            "routes",
            "events",
            "functions",
        ).map(parser::parse)

        // Then
        assertEquals(
            expected = listOf(
                Command.Help,
                Command.Help,
                Command.Back,
                Command.Reset,
                Command.Stack,
                Command.Log,
                Command.Quit,
                Command.Quit,
                Command.Routes(filter = null),
                Command.Events(viewModel = null),
                Command.Functions(viewModel = null),
            ),
            actual = commands,
        )
    }

    @Test
    fun `parses the commands that name a ViewModel or a filter`() {
        // When
        val commands = listOf(
            "routes Day",
            "events DayViewModel",
            "functions DayViewModel",
            "call Vm.reload {}",
            "settle 50",
        ).map(parser::parse)

        // Then
        assertEquals(
            expected = listOf(
                Command.Routes(filter = "Day"),
                Command.Events(viewModel = "DayViewModel"),
                Command.Functions(viewModel = "DayViewModel"),
                Command.Call(
                    target = "Vm.reload",
                    arguments = JsonObject(emptyMap()),
                    maxItems = 50,
                ),
                Command.Settle(quiet = 50.milliseconds),
            ),
            actual = commands,
        )
    }

    @Test
    fun `a command missing its target explains its usage`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("open") }

        // Then
        assertEquals(
            expected = "usage: open <Route> [{json}]",
            actual = error.message,
        )
    }

    @Test
    fun `arguments that are not a JSON object fail`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("event OnX {\"a\":1} trailing") }

        // Then
        assertEquals(
            expected = true,
            actual = error.message.orEmpty().isNotEmpty(),
        )
    }

    @Test
    fun `an unknown command points to help`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("tap 10 20") }

        // Then
        assertEquals(
            expected = "unknown command tap; run `help`",
            actual = error.message,
        )
    }

    @Test
    fun `a limit that is not a number fails`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("state --limit many") }

        // Then
        assertEquals(
            expected = "--limit takes a number",
            actual = error.message,
        )
    }
}
