package com.quare.bibleplanner.feature.releasenotes.data.mapper

import com.quare.bibleplanner.feature.releasenotes.data.model.GitHubReleaseDto
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
}
