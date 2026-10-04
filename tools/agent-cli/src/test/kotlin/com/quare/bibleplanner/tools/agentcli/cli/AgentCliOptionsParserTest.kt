package com.quare.bibleplanner.tools.agentcli.cli

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

internal class AgentCliOptionsParserTest {
    private val parser = AgentCliOptionsParser(defaultDataDirectory = File("default"))

    @Test
    fun `without options it runs the REPL on the default data directory`() {
        // When
        val options = parser.parse(emptyList())

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
    fun `reads every option`() {
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
    fun `rejects an unknown option`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(listOf("--verbose")) }

        // Then
        assertEquals(
            expected = "unknown option --verbose; options: " +
                "--fresh, --serve, --wide, --compact, --data-dir, --port, --port-file, --settle-ms",
            actual = error.message,
        )
    }

    @Test
    fun `an option without its value fails`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(listOf("--port")) }

        // Then
        assertEquals(
            expected = "--port takes a value",
            actual = error.message,
        )
    }

    @Test
    fun `a port that is not a number fails`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { parser.parse(listOf("--port", "http")) }

        // Then
        assertEquals(
            expected = "--port takes a number",
            actual = error.message,
        )
    }
}
