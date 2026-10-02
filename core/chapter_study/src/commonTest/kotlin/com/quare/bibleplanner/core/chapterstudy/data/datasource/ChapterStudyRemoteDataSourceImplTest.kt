package com.quare.bibleplanner.core.chapterstudy.data.datasource

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyContentDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.data.model.ChapterStudyStreamEvent
import com.quare.bibleplanner.core.chapterstudy.fake.chapterStudyResponse
import com.quare.bibleplanner.core.daystudy.data.datasource.StudyFunctionClient
import com.quare.bibleplanner.core.provider.supabase.testing.RecordedRequest
import com.quare.bibleplanner.core.provider.supabase.testing.SseMockEngine
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ChapterStudyRemoteDataSourceImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val request = ChapterStudyRequestDto(
        book = "GENESIS",
        chapter = 3,
        version = "NVI",
        language = "pt-BR",
    )
    private val response = chapterStudyResponse(cacheToken = "token-1")
    private lateinit var dataSource: ChapterStudyRemoteDataSourceImpl
    private lateinit var requests: MutableList<RecordedRequest>

    @Test
    fun `GIVEN progress and complete events WHEN streaming THEN it emits them in order`() = runTest {
        // Given
        prepareScenario(
            sse(
                sseEvent(
                    event = "progress",
                    data = """{"phase":"reading"}""",
                ),
                sseEvent(
                    event = "progress",
                    data = """{"phase":"summary"}""",
                ),
                sseEvent(
                    event = "complete",
                    data = json.encodeToString(
                        serializer = ChapterStudyResponseDto.serializer(),
                        value = response,
                    ),
                ),
            ),
        )

        // When
        val events = dataSource.streamChapterStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf(
                ChapterStudyStreamEvent.Progress(phase = "reading"),
                ChapterStudyStreamEvent.Progress(phase = "summary"),
                ChapterStudyStreamEvent.Complete(response = response),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a complete event as the server writes it WHEN streaming THEN it decodes the study`() = runTest {
        // Given
        prepareScenario(
            sse(
                sseEvent(
                    event = "complete",
                    data = """{"content":{"summary":"Summary","context":"Context","outline":[],""" +
                        """"peopleAndPlaces":["Eve"],"keyVerse":null,"crossReferences":[],""" +
                        """"reflectionQuestions":["Why?"],"extra":true},"model":"model-x","prompt_version":2,""" +
                        """"updated_at":"2026-10-01T10:00:00Z","is_pro":true,"client_cache_token":"token-2"}""",
                ),
            ),
        )

        // When
        val events = dataSource.streamChapterStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf<ChapterStudyStreamEvent>(
                ChapterStudyStreamEvent.Complete(
                    response = ChapterStudyResponseDto(
                        content = ChapterStudyContentDto(
                            summary = "Summary",
                            context = "Context",
                            outline = emptyList(),
                            peopleAndPlaces = listOf("Eve"),
                            keyVerse = null,
                            crossReferences = emptyList(),
                            reflectionQuestions = listOf("Why?"),
                        ),
                        model = "model-x",
                        promptVersion = 2,
                        updatedAt = "2026-10-01T10:00:00Z",
                        isPro = true,
                        clientCacheToken = "token-2",
                    ),
                ),
            ),
            actual = events,
        )
    }

    @Test
    fun `GIVEN a request WHEN streaming THEN it posts the request to the chapter study function`() = runTest {
        // Given
        prepareScenario(sse())

        // When
        dataSource.streamChapterStudy(request).toList()

        // Then
        val recorded = requests.single()
        assertEquals(
            expected = "/functions/v1/get-chapter-study",
            actual = recorded.path,
        )
        assertEquals(
            expected = json.parseToJsonElement(
                """{"book":"GENESIS","chapter":3,"version":"NVI","language":"pt-BR"}""",
            ),
            actual = json.parseToJsonElement(recorded.body),
        )
    }

    @Test
    fun `GIVEN unknown and empty events WHEN streaming THEN it skips them`() = runTest {
        // Given
        prepareScenario(
            sse(
                sseEvent(
                    event = "heartbeat",
                    data = "{}",
                ),
                "event: progress\n\n",
                ": keepalive\n\n",
                sseEvent(
                    event = "progress",
                    data = """{"phase":"questions"}""",
                ),
            ),
        )

        // When
        val events = dataSource.streamChapterStudy(request).toList()

        // Then
        assertEquals(
            expected = listOf<ChapterStudyStreamEvent>(ChapterStudyStreamEvent.Progress(phase = "questions")),
            actual = events,
        )
    }

    @Test
    fun `GIVEN an error event WHEN streaming THEN it fails with the error message`() = runTest {
        // Given
        prepareScenario(
            sse(
                sseEvent(
                    event = "error",
                    data = "generation failed",
                ),
            ),
        )

        // When
        val error = assertFailsWith<IllegalStateException> {
            dataSource.streamChapterStudy(request).toList()
        }

        // Then
        assertEquals(
            expected = "generation failed",
            actual = error.message,
        )
    }

    @Test
    fun `GIVEN a status response WHEN fetching the status THEN it decodes it`() = runTest {
        // Given
        prepareScenario(
            json(
                """{"is_unlocked":true,"used_count":2,"free_limit":3,"is_pro":false,"client_cache_token":"abc"}""",
            ),
        )

        // When
        val result = dataSource.fetchStatus(request)

        // Then
        assertEquals(
            expected = ChapterStudyStatusDto(
                isUnlocked = true,
                usedCount = 2,
                freeLimit = 3,
                isPro = false,
                clientCacheToken = "abc",
            ),
            actual = result.getOrThrow(),
        )
    }

    @Test
    fun `GIVEN a request WHEN fetching the status THEN it posts the request to the status function`() = runTest {
        // Given
        prepareScenario(
            json(
                """{"is_unlocked":false,"used_count":0,"free_limit":3,"is_pro":false,"client_cache_token":"abc"}""",
            ),
        )

        // When
        dataSource.fetchStatus(request)

        // Then
        val recorded = requests.single()
        assertEquals(
            expected = "/functions/v1/get-chapter-study-status",
            actual = recorded.path,
        )
        assertEquals(
            expected = json.parseToJsonElement(
                """{"book":"GENESIS","chapter":3,"version":"NVI","language":"pt-BR"}""",
            ),
            actual = json.parseToJsonElement(recorded.body),
        )
    }

    @Test
    fun `GIVEN the status function fails WHEN fetching the status THEN it returns a failure`() = runTest {
        // Given
        prepareScenario {
            respond(
                content = """{"error":"boom"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(
                    name = HttpHeaders.ContentType,
                    value = "application/json",
                ),
            )
        }

        // When
        val result = dataSource.fetchStatus(request)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `GIVEN a status response in another shape WHEN fetching the status THEN it returns a failure`() = runTest {
        // Given
        prepareScenario(json("""{"unexpected":true}"""))

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

    private fun prepareScenario(response: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
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
                        addHandler { requestData ->
                            recorded += RecordedRequest(
                                path = requestData.url.encodedPath,
                                body = requestData.body.toByteArray().decodeToString(),
                            )
                            response(requestData)
                        }
                    },
                ),
            )
            install(Functions)
        }
        dataSource = ChapterStudyRemoteDataSourceImpl(
            client = StudyFunctionClient(client.functions),
            json = json,
        )
    }
}
