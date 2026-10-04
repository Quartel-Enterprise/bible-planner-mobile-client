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
    fun `starts on the initial route with its ViewModels`() = runTest {
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
    fun `an event that navigates opens the next screen and logs the navigation`() = runTest {
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
    fun `open shows a route directly and back closes it`() = runTest {
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
    fun `a screen that fails to open leaves the stack and its screens as they were`() = runTest {
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
    fun `a wide window opens the study beside the day and both share the screen`() = runTest {
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
    fun `a narrow window tells the screen it is narrow`() = runTest {
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
    fun `a command waits for the change it caused even after a quiet screen`() = runTest {
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
    fun `replace swaps the top screen`() = runTest {
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
    fun `an event sent to a ViewModel updates its state`() = runTest {
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
    fun `the ViewModel calling back navigates back`() = runTest {
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
    fun `reset goes back to the initial route`() = runTest {
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
    fun `snackbars land in the log with their text`() = runTest {
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
    fun `the app-level ViewModels have their own state and take events by name`() = runTest {
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
    fun `an event two ViewModels of the screen accept must be aimed at one`() = runTest {
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
    fun `lists events, functions and routes`() = runTest {
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
    fun `a route without a known ViewModel says so`() = runTest {
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
    fun `calls a ViewModel function`() = runTest {
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
    fun `a call must name its ViewModel`() = runTest {
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
    fun `wait returns once the state reaches the value`() = runTest {
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
    fun `wait fails with the last value after its timeout`() = runTest {
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
    fun `settle, log, help and quit answer without changing the screen`() = runTest {
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
    fun `a bad command answers with an error instead of failing`() = runTest {
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
