package com.quare.bibleplanner.tools.agentcli.server

import com.quare.bibleplanner.tools.agentcli.fake.createAgentCliFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AgentCliServerTest {
    private val quit = CountDownLatch(1)

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `answers a health check`() = runTest {
        // Given
        val port = prepareScenario()

        // When
        val response = request(
            port = port,
            body = null,
        )

        // Then
        assertEquals(
            expected = Json.parseToJsonElement("""{"ok": true}"""),
            actual = Json.parseToJsonElement(response),
        )
    }

    @Test
    fun `runs one command per line and skips comments`() = runTest {
        // Given
        val port = prepareScenario()

        // When
        val response = request(
            port = port,
            body = "# where am I\nstack\n\nhelp\n",
        )

        // Then
        val responses = Json.parseToJsonElement(response) as JsonArray
        assertEquals(
            expected = listOf("true", "true"),
            actual = responses.map {
                it.jsonObject
                    .getValue("ok")
                    .jsonPrimitive.content
            },
        )
    }

    @Test
    fun `quit stops the server`() = runTest {
        // Given
        val port = prepareScenario()

        // When
        val response = request(
            port = port,
            body = "quit",
        )

        // Then
        assertTrue("bye" in response)
        assertTrue(quit.await(5, TimeUnit.SECONDS))
    }

    private fun request(
        port: Int,
        body: String?,
    ): String {
        val connection = URI("http://127.0.0.1:$port/").toURL().openConnection() as HttpURLConnection
        if (body != null) {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.outputStream.use { stream -> stream.write(body.encodeToByteArray()) }
        }
        return connection.inputStream.use { stream -> stream.readBytes().decodeToString() }
    }

    private suspend fun TestScope.prepareScenario(): Int {
        val fixture = createAgentCliFixture(
            mainDispatcher = UnconfinedTestDispatcher(testScheduler),
            isWide = false,
        )
        return AgentCliServer(
            cli = fixture.cli,
            output = Json,
            onQuit = quit::countDown,
        ).start(0)
    }
}
