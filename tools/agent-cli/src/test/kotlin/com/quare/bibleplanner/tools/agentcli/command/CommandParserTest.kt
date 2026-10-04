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
    fun `GIVEN open with JSON arguments WHEN parsing THEN reads the route and its arguments`() {
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
    fun `GIVEN replace WHEN parsing THEN opens the route replacing the top screen`() {
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
    fun `GIVEN state with a path and options WHEN parsing THEN reads all of them`() {
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
    fun `GIVEN state without options WHEN parsing THEN shows fifty items of the top screen`() {
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
    fun `GIVEN an event aimed at one ViewModel WHEN parsing THEN keeps the target and its arguments`() {
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
    fun `GIVEN a wait for a JSON value WHEN parsing THEN reads the value and its options`() {
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
    fun `GIVEN a wait value that is not JSON WHEN parsing THEN reads it as text`() {
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
    fun `GIVEN a wait value holding a double dash WHEN parsing THEN keeps the value whole`() {
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
    fun `GIVEN a wait without a value WHEN parsing THEN waits for the path to exist`() {
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
    fun `GIVEN the commands without arguments WHEN parsing THEN reads each one`() {
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
    fun `GIVEN commands naming a ViewModel or a filter WHEN parsing THEN keeps the name`() {
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
    fun `GIVEN a command missing its target WHEN parsing THEN explains its usage`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("open") }

        // Then
        assertEquals(
            expected = "usage: open <Route> [{json}]",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN arguments that are not a JSON object WHEN parsing THEN fails`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("event OnX {\"a\":1} trailing") }

        // Then
        assertEquals(
            expected = true,
            actual = error.message.orEmpty().isNotEmpty(),
        )
    }

    @Test
    fun `GIVEN an unknown command WHEN parsing THEN points to help`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("tap 10 20") }

        // Then
        assertEquals(
            expected = "unknown command tap; run `help`",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN a limit that is not a number WHEN parsing THEN fails`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse("state --limit many") }

        // Then
        assertEquals(
            expected = "--limit takes a number",
            actual = error.message,
        )
    }
}
