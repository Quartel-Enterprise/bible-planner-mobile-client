package com.quare.bibleplanner.tools.agentcli.cli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

internal class AgentCliOptionsParserTest {
    private val parser = AgentCliOptionsParser(defaultDataDirectory = File("default"))

    @Test
    fun `GIVEN no options WHEN parsing THEN runs the REPL on the default data directory`() {
        // Given
        val arguments = emptyList<String>()

        // When
        val options = parser.parse(arguments)

        // Then
        assertEquals(
            expected = AgentCliOptions(
                dataDirectory = File("default"),
                isFresh = false,
                isServing = false,
                port = 0,
                portFile = null,
                isWide = false,
                isCompact = false,
                settleQuiet = 300.milliseconds,
            ),
            actual = options,
        )
    }

    @Test
    fun `GIVEN every option WHEN parsing THEN reads each one`() {
        // Given
        val arguments = listOf(
            "--data-dir",
            "data",
            "--fresh",
            "--serve",
            "--port",
            "7457",
            "--port-file",
            "port",
            "--wide",
            "--compact",
            "--settle-ms",
            "50",
        )

        // When
        val options = parser.parse(arguments)

        // Then
        assertEquals(
            expected = AgentCliOptions(
                dataDirectory = File("data"),
                isFresh = true,
                isServing = true,
                port = 7457,
                portFile = File("port"),
                isWide = true,
                isCompact = true,
                settleQuiet = 50.milliseconds,
            ),
            actual = options,
        )
    }

    @Test
    fun `GIVEN an unknown option WHEN parsing THEN lists the known ones`() {
        // Given
        val arguments = listOf("--verbose")

        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(arguments) }

        // Then
        assertEquals(
            expected = "unknown option --verbose; options: " +
                "--fresh, --serve, --wide, --compact, --data-dir, --port, --port-file, --settle-ms",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN an option without its value WHEN parsing THEN fails`() {
        // Given
        val arguments = listOf("--port")

        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(arguments) }

        // Then
        assertEquals(
            expected = "--port takes a value",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN a port that is not a number WHEN parsing THEN fails`() {
        // Given
        val arguments = listOf("--port", "http")

        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(arguments) }

        // Then
        assertEquals(
            expected = "--port takes a number",
            actual = error.message,
        )
    }
}
