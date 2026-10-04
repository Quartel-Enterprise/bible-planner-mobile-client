package com.quare.bibleplanner.tools.agentcli.session

import com.quare.bibleplanner.tools.agentcli.fake.GenericEventViewModel
import com.quare.bibleplanner.tools.agentcli.fake.SampleUiEvent
import com.quare.bibleplanner.tools.agentcli.fake.SampleViewModel
import com.quare.bibleplanner.tools.agentcli.json.ArgumentDecoder
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.log.LogEntry
import com.quare.bibleplanner.tools.agentcli.log.LogKind
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.testTimeSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
internal class HeadlessViewModelTest {
    private val decoder = ArgumentDecoder(Json)
    private lateinit var log: SessionLog
    private lateinit var viewModel: SampleViewModel
    private lateinit var headless: HeadlessViewModel

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a ViewModel WHEN reading its state THEN shows every flow it exposes`() = runTest {
        // Given
        prepareScenario()

        // When
        val state = headless.readState(
            maxItems = 0,
            onlyProperty = null,
        )

        // Then
        assertEquals(
            expected = JsonObject(
                mapOf(
                    "greeting" to JsonPrimitive("hello"),
                    "selection" to JsonNull,
                    "uiState" to JsonPrimitive("Loading"),
                ),
            ),
            actual = state,
        )
    }

    @Test
    fun `GIVEN a ViewModel WHEN sending it an event THEN its new state shows`() = runTest {
        // Given
        prepareScenario()

        // When
        headless.send(SampleUiEvent.OnCount(amount = 2))

        // Then
        assertEquals(
            expected = Json.parseToJsonElement("""{"@type":"Loaded","items":[0,1],"label":null}"""),
            actual = headless.readState(
                maxItems = 0,
                onlyProperty = null,
            )["uiState"],
        )
    }

    @Test
    fun `GIVEN a ViewModel WHEN it emits an action THEN the action lands in the log`() = runTest {
        // Given
        prepareScenario()
        log.takeEntries()

        // When
        headless.send(SampleUiEvent.OnReset)

        // Then
        assertEquals(
            expected = listOf(
                LogEntry(
                    kind = LogKind.ACTION,
                    source = "SampleViewModel.uiAction",
                    payload = JsonPrimitive("reset"),
                ),
            ),
            actual = log.takeEntries(),
        )
    }

    @Test
    fun `GIVEN a cold flow named after messages WHEN collecting it THEN reads it as actions`() = runTest {
        // When
        prepareScenario()

        // Then
        assertEquals(
            expected = LogEntry(
                kind = LogKind.ACTION,
                source = "SampleViewModel.messages",
                payload = JsonPrimitive("toast"),
            ),
            actual = log.takeEntries().single(),
        )
    }

    @Test
    fun `GIVEN a ViewModel WHEN listing its events THEN lists every subtype`() = runTest {
        // Given
        prepareScenario()

        // When
        val eventNames = headless.eventTypes.map { it.simpleName }

        // Then
        assertEquals(
            expected = listOf("OnContent", "OnCount", "OnHolder", "OnItems", "OnRename", "OnReset", "OnStep", "OnTone"),
            actual = eventNames.sortedBy { it },
        )
    }

    @Test
    fun `GIVEN a generic base class WHEN sending an event THEN resolves the event type it declares`() = runTest {
        // Given
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val genericViewModel = GenericEventViewModel()
        val genericHeadless = HeadlessViewModel(
            viewModel = genericViewModel,
            scope = backgroundScope,
            log = SessionLog(testTimeSource),
            encoder = StateEncoder { null },
        )

        // When
        genericHeadless.send(SampleUiEvent.OnReset)

        // Then
        assertEquals(
            expected = 8,
            actual = genericHeadless.eventTypes.size,
        )
        assertEquals(
            expected = listOf<SampleUiEvent>(SampleUiEvent.OnReset),
            actual = genericViewModel.received,
        )
    }

    @Test
    fun `GIVEN public functions WHEN calling them with named arguments THEN answers their results`() = runTest {
        // Given
        prepareScenario()

        // When
        val results = listOf(
            headless.call(
                functionName = "computeDouble",
                arguments = JsonObject(mapOf("value" to JsonPrimitive(4))),
                decoder = decoder,
                maxItems = 0,
            ),
            headless.call(
                functionName = "loadGreeting",
                arguments = JsonObject(mapOf("name" to JsonPrimitive("Ana"))),
                decoder = decoder,
                maxItems = 0,
            ),
            headless.call(
                functionName = "reload",
                arguments = JsonObject(emptyMap()),
                decoder = decoder,
                maxItems = 0,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(JsonPrimitive(8), JsonPrimitive("hi Ana"), JsonNull),
            actual = results,
        )
    }

    @Test
    fun `GIVEN an unknown and an overloaded function WHEN calling them THEN both fail with their reason`() = runTest {
        // Given
        prepareScenario()

        // When
        val errors = listOf("missing", "findOverloaded").map { functionName ->
            assertFailsWith<IllegalArgumentException> {
                headless.call(
                    functionName = functionName,
                    arguments = JsonObject(emptyMap()),
                    decoder = decoder,
                    maxItems = 0,
                )
            }.message
        }

        // Then
        assertEquals(
            expected = listOf(
                "SampleViewModel has no public function missing",
                "SampleViewModel.findOverloaded is overloaded",
            ),
            actual = errors,
        )
    }

    @Test
    fun `GIVEN a ViewModel WHEN listing its functions THEN lists each with its parameters`() = runTest {
        // Given
        prepareScenario()

        // When
        val functions = headless.getFunctions(decoder)

        // Then
        assertEquals(
            expected = listOf(
                "computeDouble(value: Int)",
                "findOverloaded(value: Int)",
                "findOverloaded(value: String)",
                "loadGreeting(name: String)",
                "onEvent(event: SampleUiEvent)",
                "reload()",
            ),
            actual = functions,
        )
    }

    private fun TestScope.prepareScenario() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        log = SessionLog(testTimeSource)
        viewModel = SampleViewModel()
        headless = HeadlessViewModel(
            viewModel = viewModel,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
            log = log,
            encoder = StateEncoder { null },
        )
    }
}
