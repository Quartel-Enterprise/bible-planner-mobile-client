package com.quare.bibleplanner.core.chapterlistening.data.repository

import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioCacheDataSourceImpl
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioRemoteDataSourceImpl
import com.quare.bibleplanner.core.chapterlistening.data.mapper.ChapterAudioMapper
import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioFailureModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioResultModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioVerseModel
import com.quare.bibleplanner.core.daystudy.domain.mapper.BookIdWireNameMapper
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class ChapterAudioRepositoryImplTest {
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var directory: Path
    private lateinit var repository: ChapterAudioRepositoryImpl
    private lateinit var requests: MutableList<RecordedRequest>

    @BeforeTest
    fun setUp() {
        directory = Path(Files.createTempDirectory("chapter_audio").toString())
    }

    @AfterTest
    fun tearDown() {
        java.io.File(directory.toString()).deleteRecursively()
    }

    @Test
    fun `GIVEN a chapter not cached WHEN getting its audio THEN downloads it with the user token and caches it`() =
        runTest {
            // Given
            prepareScenario(respond = respondOk())

            // When
            val result = repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )

            // Then
            val audioPath = Path(directory, "A21_cast-pt-1_1.0.0-1_GENESIS_1.mp3").toString()
            assertEquals(
                ChapterAudioResultModel.Ready(
                    ChapterAudioModel(
                        audioPath = audioPath,
                        verses = listOf(
                            ChapterAudioVerseModel(
                                number = 1,
                                headingStart = null,
                                start = 2.seconds,
                                end = 5.5.seconds,
                            ),
                            ChapterAudioVerseModel(
                                number = 2,
                                headingStart = 6.seconds,
                                start = 7.seconds,
                                end = 10.seconds,
                            ),
                        ),
                        duration = 14.seconds,
                    ),
                ),
                result,
            )
            assertEquals(AUDIO, java.io.File(audioPath).readText())
            assertEquals("""{"book":"GENESIS","chapter":1,"version":"A21"}""", requests.first().body)
            assertEquals(
                listOf("Bearer access-token", "Bearer access-token"),
                requests.drop(1).map(RecordedRequest::authorization),
            )
        }

    @Test
    fun `GIVEN an audio URL with a query WHEN getting the audio THEN names the file after its path only`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                if (request.url.encodedPath.endsWith(".json") || request.url.host == AUDIO_HOST) {
                    respondOk()(request)
                } else {
                    respondJson(FUNCTION_RESPONSE.replace("1.mp3", "1.mp3?v=1.2"))
                }
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(
            Path(directory, "A21_cast-pt-1_1.0.0-1_GENESIS_1.mp3").toString(),
            (result as ChapterAudioResultModel.Ready).audio.audioPath,
        )
    }

    @Test
    fun `GIVEN a cached chapter WHEN getting its audio again THEN serves it without the network`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        val requestCount = requests.size

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertTrue(result is ChapterAudioResultModel.Ready)
        assertEquals(requestCount, requests.size)
    }

    @Test
    fun `GIVEN a 401 from the function WHEN getting the audio THEN asks to sign in`() = runTest {
        // Given
        prepareScenario(
            respond = respondFunction(
                status = HttpStatusCode.Unauthorized,
                body = """{"error":"Unauthorized"}""",
            ),
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.NOT_SIGNED_IN), result)
    }

    @Test
    fun `GIVEN an expired access token WHEN getting the audio THEN renews it and downloads the chapter`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                val isRenewed = request.headers[HttpHeaders.Authorization] == "Bearer renewed-token"
                when {
                    request.url.encodedPath.endsWith("/token") -> respondJson(RENEWED_SESSION)

                    isRenewed || request.url.host == AUDIO_HOST -> respondOk()(request)

                    else -> respondFunction(
                        status = HttpStatusCode.Unauthorized,
                        body = """{"error":"Unauthorized"}""",
                    )(request)
                }
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertTrue(result is ChapterAudioResultModel.Ready)
        assertEquals("Bearer renewed-token", requests.last().authorization)
    }

    @Test
    fun `GIVEN a manifest that leaves out the headings WHEN getting the audio THEN reads the verses`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                if (request.url.encodedPath.endsWith(".json")) {
                    respondJson(MANIFEST.replace(""""heading":null,""", ""))
                } else {
                    respondOk()(request)
                }
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        val audio = (result as ChapterAudioResultModel.Ready).audio
        assertEquals(null, audio.verses.first().headingStart)
    }

    @Test
    fun `GIVEN a 403 from the function WHEN getting the audio THEN says the version has no narration`() = runTest {
        // Given
        prepareScenario(
            respond = respondFunction(
                status = HttpStatusCode.Forbidden,
                body = """{"error":"off","code":"VERSION_NOT_ENABLED"}""",
            ),
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.VERSION_NOT_ENABLED), result)
    }

    @Test
    fun `GIVEN the monthly budget spent from the function WHEN getting the audio THEN says the budget is spent`() =
        runTest {
            // Given
            prepareScenario(
                respond = respondFunction(
                    status = HttpStatusCode.ServiceUnavailable,
                    body = """{"error":"spent","code":"MONTHLY_BUDGET_REACHED"}""",
                ),
            )

            // When
            val result = repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )

            // Then
            assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.BUDGET_REACHED), result)
        }

    @Test
    fun `GIVEN a generation in progress from the function WHEN getting the audio THEN says the audio is unavailable`() =
        runTest {
            // Given
            prepareScenario(
                respond = respondFunction(
                    status = HttpStatusCode.ServiceUnavailable,
                    body = """{"error":"busy","code":"GENERATION_IN_PROGRESS"}""",
                ),
            )

            // When
            val result = repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )

            // Then
            assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE), result)
        }

    @Test
    fun `GIVEN a failed generation from the function WHEN getting the audio THEN says the audio is unavailable`() =
        runTest {
            // Given
            prepareScenario(
                respond = respondFunction(
                    status = HttpStatusCode.BadGateway,
                    body = "not json",
                ),
            )

            // When
            val result = repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )

            // Then
            assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE), result)
        }

    @Test
    fun `GIVEN no connection WHEN getting an uncached chapter THEN asks for a connection`() = runTest {
        // Given
        prepareScenario(respond = { throw IOException("offline") })

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.NO_CONNECTION), result)
    }

    @Test
    fun `GIVEN no signed-in user WHEN downloading the audio THEN asks to sign in`() = runTest {
        // Given
        prepareScenario(
            respond = respondOk(),
            isSignedIn = false,
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.NOT_SIGNED_IN), result)
    }

    @Test
    fun `GIVEN the audio download fails WHEN getting the audio THEN caches nothing`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                if (request.url.encodedPath.endsWith(".mp3")) {
                    respond(content = "", status = HttpStatusCode.InternalServerError)
                } else {
                    respondOk()(request)
                }
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE), result)
        assertFalse(SystemFileSystem.exists(Path(directory, "A21_cast-pt-1_1.0.0-1_GENESIS_1.mp3")))
    }

    @Test
    fun `GIVEN the worker refuses the token WHEN downloading THEN asks to sign in`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                when {
                    request.url.host == AUDIO_HOST -> respond(content = "", status = HttpStatusCode.Unauthorized)
                    request.url.encodedPath.endsWith("/token") -> respondJson(RENEWED_SESSION)
                    else -> respondOk()(request)
                }
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.NOT_SIGNED_IN), result)
    }

    @Test
    fun `GIVEN a cached manifest that no longer reads WHEN getting the audio THEN downloads it again`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        java.io.File(directory.toString(), "A21_cast-pt-1_1.0.0-1_GENESIS_1.json").writeText("{}")
        val requestCount = requests.size

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertTrue(result is ChapterAudioResultModel.Ready)
        assertEquals(requestCount + 3, requests.size)
    }

    @Test
    fun `GIVEN a generation that outlasts the timeout WHEN getting the audio THEN says it is unavailable`() = runTest {
        // Given
        prepareScenario(
            respond = { request ->
                throw HttpRequestTimeoutException(
                    url = request.url.toString(),
                    timeoutMillis = 90_000L,
                )
            },
        )

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE), result)
    }

    @Test
    fun `GIVEN a device that cannot store the audio WHEN getting it THEN says it is unavailable`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        java.io.File(directory.toString()).deleteRecursively()
        java.io.File(directory.toString()).writeText("not a directory")

        // When
        val result = repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(ChapterAudioResultModel.Failed(ChapterAudioFailureModel.UNAVAILABLE), result)
    }

    @Test
    fun `GIVEN a cached chapter with a newer revision on the server WHEN refreshing THEN serves the new file`() =
        runTest {
            // Given
            prepareScenario(respond = respondOk())
            repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )
            prepareScenario(respond = respondOk(revision = "1.0.0-2"))

            // When
            repository.refresh(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )

            // Then
            val result = repository.getChapterAudio(
                bibleVersionId = "A21",
                chapter = genesisOne,
            )
            assertEquals(
                Path(directory, "A21_cast-pt-1_1.0.0-2_GENESIS_1.mp3").toString(),
                (result as ChapterAudioResultModel.Ready).audio.audioPath,
            )
        }

    @Test
    fun `GIVEN a cached chapter WHEN refreshing it twice THEN asks the server once`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        prepareScenario(respond = respondOk())

        // When
        repository.refresh(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        repository.refresh(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(1, requests.size)
        assertTrue(SystemFileSystem.exists(Path(directory, "A21_cast-pt-1_1.0.0-1_GENESIS_1.mp3")))
    }

    @Test
    fun `GIVEN a check that failed offline WHEN refreshing again THEN asks the server again`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        prepareScenario(respond = { throw IOException("offline") })
        repository.refresh(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        prepareScenario(respond = respondOk(revision = "1.0.0-2"))

        // When
        repository.refresh(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertTrue(SystemFileSystem.exists(Path(directory, "A21_cast-pt-1_1.0.0-2_GENESIS_1.mp3")))
    }

    @Test
    fun `GIVEN a cached chapter WHEN evicting it THEN downloads it again next time`() = runTest {
        // Given
        prepareScenario(respond = respondOk())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )
        val requestCount = requests.size

        // When
        repository.evict(Path(directory, "A21_cast-pt-1_1.0.0-1_GENESIS_1.mp3").toString())
        repository.getChapterAudio(
            bibleVersionId = "A21",
            chapter = genesisOne,
        )

        // Then
        assertEquals(requestCount * 2, requests.size)
    }

    private fun respondOk(
        revision: String = "1.0.0-1",
    ): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { request ->
        when {
            request.url.encodedPath.endsWith(".json") -> respondJson(MANIFEST)
            request.url.encodedPath.endsWith(".mp3") -> respond(content = AUDIO, status = HttpStatusCode.OK)
            else -> respondJson(FUNCTION_RESPONSE.replace("1.0.0-1", revision))
        }
    }

    private fun respondFunction(
        status: HttpStatusCode,
        body: String,
    ): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = {
        respond(
            content = body,
            status = status,
            headers = headersOf(
                name = HttpHeaders.ContentType,
                value = "application/json",
            ),
        )
    }

    private fun MockRequestHandleScope.respondJson(body: String): HttpResponseData = respond(
        content = body,
        status = HttpStatusCode.OK,
        headers = headersOf(
            name = HttpHeaders.ContentType,
            value = "application/json",
        ),
    )

    private class RecordedRequest(
        val authorization: String?,
        val body: String,
    )

    private suspend fun prepareScenario(
        respond: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
        isSignedIn: Boolean = true,
    ) {
        requests = mutableListOf()
        val recorded = requests
        val engine = MockEngine(
            MockEngineConfig().apply {
                dispatcher = Dispatchers.Unconfined
                reuseHandlers = true
                addHandler { request ->
                    recorded += RecordedRequest(
                        authorization = request.headers[HttpHeaders.Authorization],
                        body = request.body.toByteArray().decodeToString(),
                    )
                    respond(request)
                }
            },
        )
        val client = createSupabaseClient(
            supabaseUrl = "https://project.supabase.co",
            supabaseKey = "anon-key",
        ) {
            httpEngine = engine
            install(Auth) {
                alwaysAutoRefresh = false
                autoLoadFromStorage = false
                autoSaveToStorage = false
                enableLifecycleCallbacks = false
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
            install(Functions)
        }
        // Auth loads its (empty) stored session in the background, which would clear an imported one.
        client.auth.awaitInitialization()
        if (isSignedIn) {
            client.auth.importSession(
                session = UserSession(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    expiresIn = 3600,
                    tokenType = "bearer",
                    user = UserInfo(
                        aud = "authenticated",
                        id = "user-1",
                    ),
                ),
                autoRefresh = false,
            )
        }
        repository = ChapterAudioRepositoryImpl(
            remoteDataSource = ChapterAudioRemoteDataSourceImpl(
                functions = client.functions,
                httpClient = HttpClient(engine),
                auth = client.auth,
                json = json,
            ),
            cacheDataSource = ChapterAudioCacheDataSourceImpl(
                directory = ChapterAudioCacheDirectory(directory),
                maxBytes = MAX_CACHE_BYTES,
                json = json,
                currentTimestampProvider = { 0L },
            ),
            bookIdWireNameMapper = BookIdWireNameMapper(),
            chapterAudioMapper = ChapterAudioMapper(json),
            json = json,
        )
    }

    private companion object {
        const val AUDIO_HOST = "audio.test"
        const val AUDIO = "ID3 fake chapter audio"
        const val MAX_CACHE_BYTES = 10L * 1024 * 1024
        const val FUNCTION_RESPONSE =
            """{"audio_url":"https://audio.test/A21/cast-pt-1/1.0.0-1/GENESIS/1.mp3",""" +
                """"manifest_url":"https://audio.test/A21/cast-pt-1/1.0.0-1/GENESIS/1.json",""" +
                """"duration_ms":14000,"cast":"pt-1"}"""
        const val RENEWED_SESSION =
            """{"access_token":"renewed-token","refresh_token":"renewed-refresh","expires_in":3600,""" +
                """"token_type":"bearer","user":{"id":"user-1","aud":"authenticated"}}"""
        const val MANIFEST =
            """{"cast":"pt-1","audio_version":1,"duration_ms":14000,"title":null,"superscription":null,"verses":[""" +
                """{"number":1,"start_ms":2000,"duration_ms":3500,"heading":null,"parts":[],"words":[[0,50,125]]},""" +
                """{"number":2,"start_ms":7000,"duration_ms":3000,""" +
                """"heading":{"start_ms":6000,"duration_ms":800,"words":[]},"parts":[],"words":[]}]}"""
    }
}
