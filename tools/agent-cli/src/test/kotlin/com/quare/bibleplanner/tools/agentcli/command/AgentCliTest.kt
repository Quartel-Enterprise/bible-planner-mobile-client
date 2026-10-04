package com.quare.bibleplanner.tools.agentcli.command

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.tools.agentcli.fake.DayScreenViewModel
import com.quare.bibleplanner.tools.agentcli.fake.createAgentCliFixture
import com.quare.bibleplanner.tools.agentcli.session.AgentSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class AgentCliTest {
    private lateinit var trackedRoutes: List<NavKey>
    private lateinit var cli: AgentCli
    private lateinit var session: AgentSession

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a new session WHEN reading the stack THEN shows only the initial route`() = runTest {
        // Given
        prepareScenario()

        // When
        val response = send("stack")

        // Then
        assertEquals(
            expected = Json.parseToJsonElement("""[{"route": "MainNavRoute", "arguments": "MainNavRoute"}]"""),
            actual = response.result,
        )
        assertEquals(
            expected = listOf<NavKey>(MainNavRoute),
            actual = trackedRoutes,
        )
    }

    @Test
    fun `GIVEN the main screen WHEN an event navigates THEN opens the next screen and logs the navigation`() = runTest {
        // Given
        prepareScenario()

        // When
        val response = send("event OnDayClick {\"dayNumber\": 3}")

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """{"screen": "DayNavRoute", "viewModels": ["DayScreenViewModel"], "stack": "MainNavRoute > DayNavRoute"}""",
            ),
            actual = response.result,
        )
        assertEquals(
            expected = "MainNavRoute > DayNavRoute",
            actual = response.log
                .single { it.kind == "navigation" }
                .payload["stack"]
                ?.jsonPrimitive
                ?.content,
        )
        assertEquals(
            expected = JsonPrimitive("day 3"),
            actual = send("state DayScreenViewModel.uiState").result,
        )
    }

    @Test
    fun `GIVEN an opened route WHEN going back THEN closes its screen and clears its ViewModels`() = runTest {
        // Given
        prepareScenario()
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")
        val dayViewModel = session.topEntry.viewModels
            .single()
            .viewModel as DayScreenViewModel

        // When
        val response = send("back")

        // Then
        assertEquals(
            expected = "MainNavRoute",
            actual = response.result.jsonObject["stack"]
                ?.jsonPrimitive
                ?.content,
        )
        assertTrue(dayViewModel.isCleared)
    }

    @Test
    fun `GIVEN a ViewModel that fails to build WHEN replacing the top screen THEN nothing changes`() = runTest {
        // Given
        prepareScenario()
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")
        val dayViewModel = session.topEntry.viewModels
            .single()
            .viewModel as DayScreenViewModel

        // When
        val response = send("replace EditNameNavRoute")

        // Then
        assertTrue(response.error.orEmpty().endsWith(": BrokenViewModel can't be built"))
        assertEquals(
            expected = "MainNavRoute > DayNavRoute",
            actual = send("stack").result.jsonArray.joinToString(separator = " > ") { route ->
                route.jsonObject
                    .getValue("route")
                    .jsonPrimitive.content
            },
        )
        assertFalse(dayViewModel.isCleared)
    }

    @Test
    fun `GIVEN a wide window WHEN opening a day THEN the study opens beside it and both share the screen`() = runTest {
        // Given
        prepareScenario(isWide = true)

        // When
        val response = send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """{"screen": "DayStudyNavRoute", "viewModels": ["DayScreenViewModel"],
                "stack": "MainNavRoute > DayNavRoute > DayStudyNavRoute"}""",
            ),
            actual = response.result,
        )
        assertEquals(
            expected = JsonPrimitive(true),
            actual = send("state DayScreenViewModel.isWide").result,
        )
    }

    @Test
    fun `GIVEN a narrow window WHEN opening a day THEN tells the screen it is narrow`() = runTest {
        // Given
        prepareScenario()

        // When
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")

        // Then
        assertEquals(
            expected = JsonPrimitive(false),
            actual = send("state DayScreenViewModel.isWide").result,
        )
    }

    @Test
    fun `GIVEN a quiet screen WHEN an event changes the state after a delay THEN the command waits for the change`() =
        runTest {
            // Given
            prepareScenario()
            send("settle 500")

            // When
            send("event OnSlowSaveClick")

            // Then
            assertEquals(
                expected = JsonPrimitive("saved"),
                actual = send("state HomeViewModel.uiState").result,
            )
        }

    @Test
    fun `GIVEN an opened route WHEN replacing it THEN swaps the top screen`() = runTest {
        // Given
        prepareScenario()
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")

        // When
        val response = send("replace ThemeNavRoute")

        // Then
        assertEquals(
            expected = "MainNavRoute > ThemeNavRoute",
            actual = response.result.jsonObject["stack"]
                ?.jsonPrimitive
                ?.content,
        )
    }

    @Test
    fun `GIVEN an opened route WHEN sending an event to its ViewModel THEN its state updates`() = runTest {
        // Given
        prepareScenario()
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")

        // When
        send("event DayScreenViewModel.OnCount")

        // Then
        assertEquals(
            expected = JsonPrimitive(1),
            actual = send("state DayScreenViewModel.count").result,
        )
    }

    @Test
    fun `GIVEN an opened route WHEN its ViewModel navigates back THEN the screen closes and it is logged`() = runTest {
        // Given
        prepareScenario()
        send("open DayNavRoute {\"dayNumber\": 2, \"weekNumber\": 1, \"readingPlanType\": \"BOOKS\"}")

        // When
        val response = send("event OnBackClick")

        // Then
        assertEquals(
            expected = listOf<NavKey>(MainNavRoute),
            actual = session.stack,
        )
        assertEquals(
            expected = "navigation",
            actual = response.log.single().kind,
        )
    }

    @Test
    fun `GIVEN an opened route WHEN resetting THEN goes back to the initial route`() = runTest {
        // Given
        prepareScenario()
        send("open ThemeNavRoute")

        // When
        val response = send("reset")

        // Then
        assertEquals(
            expected = "MainNavRoute",
            actual = response.result.jsonObject["stack"]
                ?.jsonPrimitive
                ?.content,
        )
    }

    @Test
    fun `GIVEN the main screen WHEN an event shows a snackbar THEN the log holds it with its text`() = runTest {
        // Given
        prepareScenario()

        // When
        val response = send("event OnSnackbarClick")

        // Then
        assertEquals(
            expected = Json.parseToJsonElement(
                """{"stringResource": {"@string": "saved", "text": "text of saved"}, "isDismissible": true}""",
            ),
            actual = response.log.single { it.kind == "snackbar" }.payload,
        )
    }

    @Test
    fun `GIVEN the app-level ViewModels WHEN reading their state and sending them an event THEN both work by name`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val state = send("state --app").result
            val event = send("event AppLevelViewModel.OnReset")

            // Then
            assertEquals(
                expected = Json.parseToJsonElement("""{"AppLevelViewModel": {"theme": "SYSTEM"}}"""),
                actual = state,
            )
            assertTrue(event.isOk)
        }

    @Test
    fun `GIVEN two ViewModels accepting the same event WHEN sending it unqualified THEN asks to name one`() = runTest {
        // Given
        prepareScenario()
        send("open ThemeNavRoute")

        // When
        val response = send("event OnReset")

        // Then
        assertEquals(
            expected = "OnReset exists on [SampleViewModel, AppLevelViewModel]; use ViewModel.OnReset",
            actual = response.error,
        )
    }

    @Test
    fun `GIVEN the main screen WHEN listing events functions and routes THEN describes each with its arguments`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val events = send("events HomeViewModel").result
            val functions = send("functions HomeViewModel").result
            val routes = send("routes DayNavRoute").result

            // Then
            assertEquals(
                expected = Json.parseToJsonElement(
                    """{"HomeViewModel": ["OnDayClick(dayNumber: Int)", "OnSlowSaveClick", "OnSnackbarClick"]}""",
                ),
                actual = events,
            )
            assertEquals(
                expected = Json.parseToJsonElement("""{"HomeViewModel": ["onEvent(event: HomeUiEvent)"]}"""),
                actual = functions,
            )
            assertEquals(
                expected = Json.parseToJsonElement(
                    """["DayNavRoute(dayNumber: Int, weekNumber: Int, readingPlanType: String) -> DayScreenViewModel"]""",
                ),
                actual = routes,
            )
        }

    @Test
    fun `GIVEN a route without a known ViewModel WHEN listing it THEN says so`() = runTest {
        // Given
        prepareScenario()

        // When
        val routes = send("routes LogoutNavRoute").result

        // Then
        assertEquals(
            expected = Json.parseToJsonElement("""["LogoutNavRoute -> no ViewModel on the JVM"]"""),
            actual = routes,
        )
    }

    @Test
    fun `GIVEN a public ViewModel function WHEN calling it THEN answers its result`() = runTest {
        // Given
        prepareScenario()

        // When
        val response = send("call SampleViewModel.computeDouble {\"value\": 21}")

        // Then
        assertEquals(
            expected = JsonPrimitive(42),
            actual = response.result,
        )
    }

    @Test
    fun `GIVEN a call without a ViewModel WHEN running it THEN explains the usage`() = runTest {
        // Given
        prepareScenario()

        // When
        val response = send("call computeDouble {\"value\": 21}")

        // Then
        assertEquals(
            expected = "usage: call ViewModel.function [{json}]",
            actual = response.error,
        )
    }

    @Test
    fun `GIVEN a state already at the value WHEN waiting for it THEN returns the value`() = runTest {
        // Given
        prepareScenario()
        send("event SampleViewModel.OnCount {\"amount\": 1}")

        // When
        val response = send("wait SampleViewModel.uiState.@type = Loaded --timeout 1000")

        // Then
        assertEquals(
            expected = JsonPrimitive("Loaded"),
            actual = response.result,
        )
    }

    @Test
    fun `GIVEN a state that never reaches the value WHEN waiting THEN fails with the last value after the timeout`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val response = send("wait SampleViewModel.uiState = Loaded --timeout 200")

            // Then
            assertEquals(
                expected = "timed out after 200ms waiting for SampleViewModel.uiState; last value: \"Loading\"",
                actual = response.error,
            )
        }

    @Test
    fun `GIVEN the main screen WHEN running settle log help and quit THEN each answers without changing the screen`() =
        runTest {
            // Given
            prepareScenario()

            // When
            val settle = send("settle 50")
            val log = send("log")
            val help = send("help")
            val quit = cli.run("quit")

            // Then
            assertEquals(
                expected = JsonPrimitive(true),
                actual = settle.result,
            )
            assertEquals(
                expected = JsonArray(emptyList()),
                actual = log.result,
            )
            assertTrue(help.result.jsonArray.isNotEmpty())
            assertTrue(quit.isQuit)
        }

    @Test
    fun `GIVEN bad commands WHEN running them THEN each answers with an error`() = runTest {
        // Given
        prepareScenario()

        // When
        val errors = listOf("tap", "open Nowhere", "event OnNothing", "state Nope", "events Nope")
            .map { line -> send(line) }

        // Then
        assertEquals(
            expected = listOf(
                "unknown command tap; run `help`",
                "unknown route Nowhere; run `routes` to list them",
                "no event OnNothing on this screen; run `events`",
                "no \"Nope\" at Nope; keys: [HomeViewModel, SampleViewModel]",
                "no Nope here; this screen has [HomeViewModel, SampleViewModel, AppLevelViewModel]",
            ),
            actual = errors.map(Response::error),
        )
        assertFalse(errors.any(Response::isOk))
    }

    private suspend fun send(line: String): Response {
        val json = cli.run(line).json.jsonObject
        return Response(
            isOk = json["ok"]?.jsonPrimitive?.content == "true",
            result = json["result"] ?: JsonNull,
            error = json["error"]?.jsonPrimitive?.content,
            log = json["log"]?.jsonArray.orEmpty().map { entry ->
                LogLine(
                    kind = entry.jsonObject
                        .getValue("kind")
                        .jsonPrimitive.content,
                    payload = entry.jsonObject.getValue("payload") as? JsonObject ?: JsonObject(emptyMap()),
                )
            },
        )
    }

    private suspend fun TestScope.prepareScenario(isWide: Boolean = false) {
        val fixture = createAgentCliFixture(
            mainDispatcher = StandardTestDispatcher(testScheduler),
            isWide = isWide,
        )
        cli = fixture.cli
        session = fixture.session
        trackedRoutes = fixture.trackedRoutes
    }

    private class Response(
        val isOk: Boolean,
        val result: JsonElement,
        val error: String?,
        val log: List<LogLine>,
    )

    private class LogLine(
        val kind: String,
        val payload: JsonObject,
    )
}
