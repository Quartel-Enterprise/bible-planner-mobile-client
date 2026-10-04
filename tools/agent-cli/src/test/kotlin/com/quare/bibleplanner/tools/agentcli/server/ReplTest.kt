package com.quare.bibleplanner.tools.agentcli.server

import com.quare.bibleplanner.tools.agentcli.fake.createAgentCliFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class ReplTest {
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `answers each line with one JSON line and stops at quit`() = runTest {
        // Given
        val fixture = createAgentCliFixture(
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
            isWide = false,
        )
        val output = ByteArrayOutputStream()

        // When
        Repl(
            cli = fixture.cli,
            output = Json,
            input = "# comment\nstack\n\nquit\nstack\n".reader().buffered(),
            responses = PrintStream(output, true, Charsets.UTF_8),
        ).run()

        // Then
        assertEquals(
            expected = listOf("true", "true"),
            actual = output
                .toString(Charsets.UTF_8)
                .lines()
                .filter(String::isNotBlank)
                .map { line ->
                    Json
                        .parseToJsonElement(line)
                        .jsonObject
                        .getValue("ok")
                        .jsonPrimitive.content
                },
        )
    }
}
