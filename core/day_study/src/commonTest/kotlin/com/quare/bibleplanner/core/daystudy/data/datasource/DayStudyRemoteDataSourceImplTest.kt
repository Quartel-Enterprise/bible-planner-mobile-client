package com.quare.bibleplanner.core.daystudy.data.datasource

import com.quare.bibleplanner.core.daystudy.data.dto.ChapterRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.core.daystudy.data.dto.PassageRequestDto
import com.quare.bibleplanner.core.daystudy.data.exception.DayStudyStreamStalledException
import com.quare.bibleplanner.core.daystudy.data.model.DayStudyStreamEvent
import com.quare.bibleplanner.core.daystudy.fake.RecordedRequest
import com.quare.bibleplanner.core.daystudy.fake.SseMockEngine
import com.quare.bibleplanner.core.daystudy.fake.dayStudyResponse
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class DayStudyRemoteDataSourceImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val request = DayStudyRequestDto(
        passages = listOf(
            PassageRequestDto(
                book = "GEN",
                chapters = listOf(
                    ChapterRequestDto(
                        number = 1,
                        startVerse = null,
                        endVerse = null,
                    ),
                ),
            ),
        ),
        version = "NVI",
        language = "pt",
    )
    private val response = dayStudyResponse(cacheToken = "token-1")
    private lateinit var dataSource: DayStudyRemoteDataSourceImpl
    private lateinit var requests: MutableList<RecordedRequest>

    @Test
    fun `GIVEN progress and complete events WHEN streaming THEN it emits them in order`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                sse(
                    sseEvent(
                        event = "progress",
                        data = """{"phase":"reading"}""",
                    ),
                    sseEvent(
                        event = "complete",
                        data = json.encodeToString(DayStudyResponseDto.serializer(), response),
                    ),
                ),
            ),
        )

        // When
        val events = dataSource.streamDayStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf(
                DayStudyStreamEvent.Progress(phase = "reading"),
                DayStudyStreamEvent.Complete(response = response),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a request WHEN streaming THEN it posts the request to the day study function`() = runTest {
        // Given
        prepareScenario(responses = listOf(sse()))

        // When
        dataSource.streamDayStudy(request).toList()

        // Then
        val recorded = requests.single()
        assertEquals(
            expected = "/functions/v1/get-day-study",
            actual = recorded.path,
        )
        assertEquals(
            expected = json.parseToJsonElement(
                """{"passages":[{"book":"GEN","chapters":[{"number":1}]}],"version":"NVI","language":"pt"}""",
            ),
            actual = json.parseToJsonElement(recorded.body),
        )
    }

    @Test
    fun `GIVEN unknown and empty events WHEN streaming THEN it skips them`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                sse(
                    sseEvent(
                        event = "heartbeat",
                        data = "{}",
                    ),
                    "event: progress\n\n",
                    sseEvent(
                        event = "progress",
                        data = """{"phase":"writing"}""",
                    ),
                ),
            ),
        )

        // When
        val events = dataSource.streamDayStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf(DayStudyStreamEvent.Progress(phase = "writing")),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an error event WHEN streaming THEN it fails with the error message`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                sse(
                    sseEvent(
                        event = "error",
                        data = "generation failed",
                    ),
                ),
            ),
        )

        // When
        val error = assertFailsWith<IllegalStateException> {
            dataSource.streamDayStudy(request).toList()
        }

        // Then
        assertEquals(
            expected = "generation failed",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN the connection fails before any event WHEN streaming THEN it retries once`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                { throw IOException("connection reset") },
                sse(
                    sseEvent(
                        event = "progress",
                        data = """{"phase":"reading"}""",
                    ),
                ),
            ),
        )

        // When
        val events = dataSource.streamDayStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf(DayStudyStreamEvent.Progress(phase = "reading")),
            actual = events,
        )
        assertEquals(
            expected = 2,
            actual = requests.size,
        )
    }

    @Test
    fun `GIVEN the connection keeps failing WHEN streaming THEN it gives up after one retry`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                { throw IOException("connection reset") },
                { throw IOException("connection reset") },
            ),
        )

        // When
        assertFailsWith<Exception> {
            dataSource.streamDayStudy(request).toList()
        }

        // Then
        assertEquals(
            expected = 2,
            actual = requests.size,
        )
    }

    @Test
    fun `GIVEN the stream sends nothing WHEN the idle timeout passes THEN it fails as stalled`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                {
                    respond(
                        content = ByteChannel(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(
                            name = HttpHeaders.ContentType,
                            value = "text/event-stream",
                        ),
                    )
                },
            ),
        )

        // When
        val result = runCatching { dataSource.streamDayStudy(request).toList() }

        // Then
        assertTrue(result.exceptionOrNull() is DayStudyStreamStalledException)
    }

    @Test
    fun `GIVEN a status response WHEN fetching the status THEN it decodes it`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                json(
                    """{"is_unlocked":true,"used_count":2,"free_limit":3,"is_pro":false,"client_cache_token":"abc"}""",
                ),
            ),
        )

        // When
        val result = dataSource.fetchStatus(request)

        // Then
        assertEquals(
            expected = DayStudyStatusDto(
                isUnlocked = true,
                usedCount = 2,
                freeLimit = 3,
                isPro = false,
                clientCacheToken = "abc",
            ),
            actual = result.getOrThrow(),
        )
        assertEquals(
            expected = "/functions/v1/get-day-study-status",
            actual = requests.single().path,
        )
    }

    @Test
    fun `GIVEN the status function fails WHEN fetching the status THEN it returns a failure`() = runTest {
        // Given
        prepareScenario(
            responses = listOf(
                {
                    respond(
                        content = """{"error":"boom"}""",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(
                            name = HttpHeaders.ContentType,
                            value = "application/json",
                        ),
                    )
                },
            ),
        )

        // When
        val result = dataSource.fetchStatus(request)

        // Then
        assertTrue(result.isFailure)
    }

    private fun sseEvent(
        event: String,
        data: String,
    ): String = "event: $event\ndata: $data\n\n"

    private fun sse(vararg events: String): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respond(
            content = events.joinToString(separator = ""),
            status = HttpStatusCode.OK,
            headers = headersOf(
                name = HttpHeaders.ContentType,
                value = "text/event-stream",
            ),
        )
    }

    private fun json(body: String): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(
                name = HttpHeaders.ContentType,
                value = "application/json",
            ),
        )
    }

    private fun prepareScenario(responses: List<suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData>) {
        requests = mutableListOf()
        val recorded = requests
        val client = createSupabaseClient(
            supabaseUrl = "https://project.supabase.co",
            supabaseKey = "anon-key",
        ) {
            httpEngine = SseMockEngine(
                MockEngine(
                    MockEngineConfig().apply {
                        dispatcher = Dispatchers.Unconfined
                        responses.forEach { response ->
                            addHandler { requestData ->
                                recorded += RecordedRequest(
                                    path = requestData.url.encodedPath,
                                    body = requestData.body.toByteArray().decodeToString(),
                                )
                                response(requestData)
                            }
                        }
                    },
                ),
            )
            install(Functions)
        }
        dataSource = DayStudyRemoteDataSourceImpl(
            functions = client.functions,
            json = json,
        )
    }
}
