package com.quare.bibleplanner.feature.releasenotes.data.repository

import bibleplanner.feature.release_notes.generated.resources.Res
import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import com.quare.bibleplanner.core.utils.json_reader.JsonResourceReader
import com.quare.bibleplanner.core.utils.locale.AppLanguage
import com.quare.bibleplanner.core.utils.locale.getCurrentLanguage
import com.quare.bibleplanner.feature.releasenotes.data.mapper.GitHubReleaseDateMapper
import com.quare.bibleplanner.feature.releasenotes.data.model.GitHubReleaseDto
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import com.quare.bibleplanner.feature.releasenotes.domain.repository.ReleaseNotesRepository
import com.quare.bibleplanner.feature.releasenotes.generated.ReleaseNotesBuildKonfig
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate

class GitHubReleaseNotesRepository(
    private val requestHandler: RequestHandler,
    private val jsonResourceReader: JsonResourceReader,
    private val releaseDateMapper: GitHubReleaseDateMapper,
) : ReleaseNotesRepository {
    override suspend fun getReleaseNotes(): Result<List<ReleaseNoteModel>> = coroutineScope {
        val datesDeferred = async { getReleaseDates() }
        val fileName = when (getCurrentLanguage()) {
            AppLanguage.PORTUGUESE_BRAZIL -> "pt.json"
            AppLanguage.SPANISH -> "es.json"
            AppLanguage.ENGLISH -> "en.json"
        }

        val path = "files/release_notes/$fileName"
        val jsonDeferred = async {
            jsonResourceReader.read<Map<String, List<String>>>(path) {
                Res.readBytes(it)
            }
        }

        try {
            val map = jsonDeferred.await()
            val datesResult = datesDeferred.await()
            val dates = datesResult.getOrDefault(emptyMap())

            val notes = releaseDateMapper.mapToReleaseNoteModels(map, dates)

            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getReleaseDates(): Result<Map<String, LocalDate>> = requestHandler
        .call<List<GitHubReleaseDto>> {
            get(REQUEST_URL) {
                val token = ReleaseNotesBuildKonfig.GH_TOKEN
                if (token.isNotBlank()) {
                    header(
                        key = HttpHeaders.Authorization,
                        value = "Bearer $token",
                    )
                }
                header(
                    key = HttpHeaders.Accept,
                    value = "application/vnd.github+json",
                )
                header(
                    key = "X-GitHub-Api-Version",
                    value = "2022-11-28",
                )
                url {
                    parameters.append("per_page", "100")
                }
            }
        }.map(releaseDateMapper::mapToReleaseDates)

    companion object {
        private const val REQUEST_URL =
            "https://api.github.com/repos/Quartel-Enterprise/bible-planner-mobile-client/releases"
    }
}
