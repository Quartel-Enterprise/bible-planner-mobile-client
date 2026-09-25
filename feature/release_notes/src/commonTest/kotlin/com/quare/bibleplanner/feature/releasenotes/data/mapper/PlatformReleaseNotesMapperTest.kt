package com.quare.bibleplanner.feature.releasenotes.data.mapper

import com.quare.bibleplanner.core.provider.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformReleaseNotesMapperTest {
    private val releaseNotes = mapOf(
        "2.9.0" to mapOf(
            "common" to listOf("common change"),
            "android" to listOf("android change"),
            "ios" to listOf("ios change"),
            "desktop" to listOf("desktop change"),
        ),
    )

    private lateinit var mapper: PlatformReleaseNotesMapper

    @Test
    fun `GIVEN Android WHEN mapping THEN keeps the common changes followed by the Android ones`() {
        // Given
        prepareScenario(platform = Platform.Android)

        // When
        val changes = mapper.mapToPlatformChanges(releaseNotes)

        // Then
        assertEquals(
            expected = mapOf("2.9.0" to listOf("common change", "android change")),
            actual = changes,
        )
    }

    @Test
    fun `GIVEN iOS WHEN mapping THEN keeps the common changes followed by the iOS ones`() {
        // Given
        prepareScenario(platform = Platform.Ios)

        // When
        val changes = mapper.mapToPlatformChanges(releaseNotes)

        // Then
        assertEquals(
            expected = mapOf("2.9.0" to listOf("common change", "ios change")),
            actual = changes,
        )
    }

    @Test
    fun `GIVEN any desktop OS WHEN mapping THEN keeps the common changes followed by the desktop ones`() {
        // Given
        val desktops = listOf(
            Platform.Desktop.MacOs,
            Platform.Desktop.Linux,
            Platform.Desktop.Windows,
        )

        // When
        val changesPerDesktop = desktops.map { desktop ->
            prepareScenario(platform = desktop)
            mapper.mapToPlatformChanges(releaseNotes)
        }

        // Then
        changesPerDesktop.forEach { changes ->
            assertEquals(
                expected = mapOf("2.9.0" to listOf("common change", "desktop change")),
                actual = changes,
            )
        }
    }

    @Test
    fun `GIVEN a version without common changes WHEN mapping THEN keeps only the platform ones`() {
        // Given
        prepareScenario(platform = Platform.Ios)
        val iosOnly = mapOf(
            "2.8.5" to mapOf("ios" to listOf("ios change")),
        )

        // When
        val changes = mapper.mapToPlatformChanges(iosOnly)

        // Then
        assertEquals(
            expected = mapOf("2.8.5" to listOf("ios change")),
            actual = changes,
        )
    }

    @Test
    fun `GIVEN a version with changes only for other platforms WHEN mapping THEN drops the version`() {
        // Given
        prepareScenario(platform = Platform.Ios)
        val mixed = mapOf(
            "2.8.4" to mapOf("android" to listOf("android change")),
            "2.8.3" to mapOf("common" to listOf("common change")),
        )

        // When
        val changes = mapper.mapToPlatformChanges(mixed)

        // Then
        assertEquals(
            expected = mapOf("2.8.3" to listOf("common change")),
            actual = changes,
        )
    }

    private fun prepareScenario(platform: Platform) {
        mapper = PlatformReleaseNotesMapper(platform)
    }
}
