package com.quare.bibleplanner.feature.releasenotes.data.mapper

import com.quare.bibleplanner.core.date.toDateRepresentation
import com.quare.bibleplanner.feature.releasenotes.data.model.GitHubReleaseDto
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class GitHubReleaseDateMapper {
    fun mapToReleaseDates(releases: List<GitHubReleaseDto>): Map<String, LocalDate> = releases
        .mapNotNull { release ->
            val date = try {
                release.publishedAt
                    ?.let(Instant::parse)
                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                    ?.date
            } catch (_: Exception) {
                null
            }
            date?.let { release.tagName to it }
        }.toMap()

    fun mapToReleaseNoteModels(
        releaseNotesMap: Map<String, List<String>>,
        dates: Map<String, LocalDate>,
    ): List<ReleaseNoteModel> = releaseNotesMap.map { (version, changes) ->
        val date = dates[version] ?: dates["v$version"]
        ReleaseNoteModel(
            version = version,
            changes = changes,
            dateRepresentation = date?.toDateRepresentation(),
        )
    }
}
