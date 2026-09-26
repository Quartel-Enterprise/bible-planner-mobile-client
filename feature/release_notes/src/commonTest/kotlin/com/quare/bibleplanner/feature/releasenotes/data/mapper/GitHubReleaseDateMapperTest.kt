package com.quare.bibleplanner.feature.releasenotes.data.mapper

import com.quare.bibleplanner.core.date.toDateRepresentation
import com.quare.bibleplanner.feature.releasenotes.data.model.GitHubReleaseDto
import com.quare.bibleplanner.feature.releasenotes.domain.model.ReleaseNoteModel
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GitHubReleaseDateMapperTest {
    private val mapper = GitHubReleaseDateMapper()

    @Test
    fun `GIVEN a stable release with a valid published date WHEN mapping THEN maps the tag to its local date`() {
        // Given
        val releases = listOf(
            GitHubReleaseDto(
                tagName = "2.3.0",
                isPrerelease = false,
                publishedAt = "2026-08-01T12:00:00Z",
                body = null,
            ),
        )

        // When
        val dates = mapper.mapToReleaseDates(releases)

        // Then
        assertEquals(
            expected = LocalDate(
                year = 2026,
                month = 8,
                day = 1,
            ),
            actual = dates["2.3.0"],
        )
    }

    @Test
    fun `GIVEN a prerelease entry WHEN mapping THEN excludes it`() {
        // Given
        val releases = listOf(
            GitHubReleaseDto(
                tagName = "2.4.0-beta-1",
                isPrerelease = true,
                publishedAt = "2026-08-02T12:00:00Z",
                body = null,
            ),
            GitHubReleaseDto(
                tagName = "2.3.0",
                isPrerelease = false,
                publishedAt = "2026-08-01T12:00:00Z",
                body = null,
            ),
        )

        // When
        val dates = mapper.mapToReleaseDates(releases)

        // Then
        assertEquals(
            expected = setOf("2.3.0"),
            actual = dates.keys,
        )
    }

    @Test
    fun `GIVEN entries with null or invalid published dates WHEN mapping THEN drops them`() {
        // Given
        val releases = listOf(
            GitHubReleaseDto(
                tagName = "2.2.0",
                isPrerelease = false,
                publishedAt = null,
                body = null,
            ),
            GitHubReleaseDto(
                tagName = "2.1.2",
                isPrerelease = false,
                publishedAt = "not-a-date",
                body = null,
            ),
        )

        // When
        val dates = mapper.mapToReleaseDates(releases)

        // Then
        assertTrue(dates.isEmpty())
    }

    @Test
    fun `GIVEN dates tagged with or without a v prefix WHEN mapping the notes THEN dates each version`() {
        // Given
        val releaseNotesMap = mapOf(
            "2.4.0" to listOf("New reading plan"),
            "2.3.0" to listOf("Bug fixes"),
            "2.2.0" to listOf("Faster sync"),
        )
        val dates = mapOf(
            "2.4.0" to LocalDate(year = 2020, month = 2, day = 1),
            "v2.3.0" to LocalDate(year = 2020, month = 1, day = 1),
        )

        // When
        val notes = mapper.mapToReleaseNoteModels(
            releaseNotesMap = releaseNotesMap,
            dates = dates,
        )

        // Then
        assertEquals(
            expected = listOf(
                ReleaseNoteModel(
                    version = "2.4.0",
                    changes = listOf("New reading plan"),
                    dateRepresentation = LocalDate(year = 2020, month = 2, day = 1).toDateRepresentation(),
                ),
                ReleaseNoteModel(
                    version = "2.3.0",
                    changes = listOf("Bug fixes"),
                    dateRepresentation = LocalDate(year = 2020, month = 1, day = 1).toDateRepresentation(),
                ),
                ReleaseNoteModel(
                    version = "2.2.0",
                    changes = listOf("Faster sync"),
                    dateRepresentation = null,
                ),
            ),
            actual = notes,
        )
    }
}
