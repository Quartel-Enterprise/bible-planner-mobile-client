package com.quare.bibleplanner.feature.releasenotes.data.repository

import com.quare.bibleplanner.core.date.toDateRepresentation
import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import com.quare.bibleplanner.core.provider.language.domain.provider.LanguageProvider
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.jsonreader.JsonResourceReader
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.releasenotes.data.mapper.GitHubReleaseDateMapper
import com.quare.bibleplanner.feature.releasenotes.data.mapper.PlatformReleaseNotesMapper
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class GitHubReleaseNotesRepositoryTest {
    private lateinit var repository: GitHubReleaseNotesRepository
    private lateinit var requests: List<HttpRequestData>

    private val firstReleaseDate = LocalDate(year = 2024, month = 3, day = 10)

    @Test
    fun `GIVEN published releases on GitHub WHEN getting the release notes THEN dates the versions GitHub knows`() =
        runTest {
            // Given
            prepareScenario(language = Language.ENGLISH)

            // When
            val notes = repository.getReleaseNotes().getOrThrow()

            // Then
            assertEquals(
                ReleaseNoteModel(
                    version = FIRST_VERSION,
                    changes = listOf("The first version of Bible Planner 🚀."),
                    dateRepresentation = firstReleaseDate.toDateRepresentation(),
                ),
                notes.single { it.version == FIRST_VERSION },
            )
            assertTrue(notes.filterNot { it.version == FIRST_VERSION }.all { it.dateRepresentation == null })
        }

    @Test
    fun `GIVEN the GitHub request WHEN getting the release notes THEN asks the GitHub API for up to 100 releases`() =
        runTest {
            // Given
            prepareScenario(language = Language.ENGLISH)

            // When
            repository.getReleaseNotes()

            // Then
            val request = requests.single()
            assertEquals("application/vnd.github+json", request.headers[HttpHeaders.Accept])
            assertEquals("2022-11-28", request.headers["X-GitHub-Api-Version"])
            assertEquals("100", request.url.parameters["per_page"])
        }

    @Test
    fun `GIVEN GitHub is unavailable WHEN getting the release notes THEN still returns the notes without dates`() =
        runTest {
            // Given
            prepareScenario(
                language = Language.ENGLISH,
                gitHubStatus = HttpStatusCode.ServiceUnavailable,
            )

            // When
            val notes = repository.getReleaseNotes().getOrThrow()

            // Then
            assertTrue(notes.isNotEmpty())
            assertNull(notes.single { it.version == FIRST_VERSION }.dateRepresentation)
        }

    @Test
    fun `GIVEN brazilian portuguese WHEN getting the release notes THEN reads the portuguese notes`() = runTest {
        // Given
        prepareScenario(language = Language.PORTUGUESE_BRAZIL)

        // When
        val notes = repository.getReleaseNotes().getOrThrow()

        // Then
        assertEquals(
            listOf("A primeira versão do Bible Planner 🚀."),
            notes.single { it.version == FIRST_VERSION }.changes,
        )
    }

    @Test
    fun `GIVEN spanish WHEN getting the release notes THEN reads the spanish notes`() = runTest {
        // Given
        prepareScenario(language = Language.SPANISH)

        // When
        val notes = repository.getReleaseNotes().getOrThrow()

        // Then
        assertEquals(
            listOf("La primera versão de Bible Planner 🚀."),
            notes.single { it.version == FIRST_VERSION }.changes,
        )
    }

    private fun prepareScenario(
        language: Language,
        gitHubStatus: HttpStatusCode = HttpStatusCode.OK,
    ) {
        val recordedRequests = mutableListOf<HttpRequestData>()
        requests = recordedRequests
        val publishedAt = firstReleaseDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val engine = MockEngine { request ->
            recordedRequests += request
            respond(
                content =
                    """
                    [{"tag_name":"v$FIRST_VERSION","prerelease":false,"published_at":"$publishedAt","body":null}]
                    """.trimIndent(),
                status = gitHubStatus,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        repository = GitHubReleaseNotesRepository(
            requestHandler = RequestHandler(httpClient),
            jsonResourceReader = JsonResourceReader(),
            releaseDateMapper = GitHubReleaseDateMapper(),
            platformReleaseNotesMapper = PlatformReleaseNotesMapper(Platform.Android),
            languageProvider = FixedLanguageProvider(language),
        )
    }

    private companion object {
        const val FIRST_VERSION = "1.0.0"
    }
}

private class FixedLanguageProvider(
    private val appLanguage: Language,
) : LanguageProvider {
    override fun getDeviceLanguage(): Language = error("unused")

    override fun getAppLanguage(): Language = appLanguage
}
