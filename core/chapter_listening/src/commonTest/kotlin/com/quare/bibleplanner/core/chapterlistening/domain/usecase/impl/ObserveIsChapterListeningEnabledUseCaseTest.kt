package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterAudioPlayerEvent
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration

internal class ObserveIsChapterListeningEnabledUseCaseTest {
    private lateinit var useCase: ObserveIsChapterListeningEnabledUseCase
    private lateinit var booleanRemoteConfig: FakeObserveBooleanRemoteConfig
    private lateinit var stringRemoteConfig: FakeObserveStringRemoteConfig

    @Test
    fun `GIVEN a platform without an audio player WHEN observing THEN listening is off`() = runTest {
        // Given
        prepareScenario(
            isSupported = false,
            isEnabled = true,
            cloudVersions = null,
            selectedVersionId = "a21",
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertFalse(isEnabled)
    }

    @Test
    fun `GIVEN a supported platform WHEN observing THEN follows the kill switch which defaults to off`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            isEnabled = true,
            cloudVersions = null,
            selectedVersionId = "a21",
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertTrue(isEnabled)
        assertEquals("chapter_listening_enabled" to false, booleanRemoteConfig.requests.single())
        assertEquals("listening_cloud_versions", stringRemoteConfig.requests.single().first)
    }

    @Test
    fun `GIVEN the kill switch off WHEN observing THEN listening is off`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            isEnabled = false,
            cloudVersions = null,
            selectedVersionId = "a21",
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertFalse(isEnabled)
    }

    @Test
    fun `GIVEN a version outside the remote list WHEN observing THEN listening is off`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            isEnabled = true,
            cloudVersions = " A21 , niv",
            selectedVersionId = "rvr",
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertFalse(isEnabled)
    }

    @Test
    fun `GIVEN a version in the remote list WHEN observing THEN matches it ignoring case and spaces`() = runTest {
        // Given
        prepareScenario(
            isSupported = true,
            isEnabled = true,
            cloudVersions = " A21 , niv",
            selectedVersionId = "NIV",
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertTrue(isEnabled)
    }

    private fun prepareScenario(
        isSupported: Boolean,
        isEnabled: Boolean,
        cloudVersions: String?,
        selectedVersionId: String,
    ) {
        booleanRemoteConfig = FakeObserveBooleanRemoteConfig(isEnabled)
        stringRemoteConfig = FakeObserveStringRemoteConfig(cloudVersions)
        useCase = ObserveIsChapterListeningEnabledUseCase(
            audioPlayer = SupportAudioPlayer(isSupported),
            getSelectedVersionIdFlow = { flowOf(selectedVersionId) },
            observeBooleanRemoteConfig = booleanRemoteConfig,
            observeStringRemoteConfig = stringRemoteConfig,
        )
    }
}

private class FakeObserveBooleanRemoteConfig(
    private val value: Boolean,
) : ObserveBooleanRemoteConfig {
    val requests = mutableListOf<Pair<String, Boolean>>()

    override fun invoke(
        key: String,
        default: Boolean,
    ): Flow<Boolean> {
        requests += key to default
        return flowOf(value)
    }
}

private class FakeObserveStringRemoteConfig(
    private val value: String?,
) : ObserveStringRemoteConfig {
    val requests = mutableListOf<Pair<String, String>>()

    override fun invoke(
        key: String,
        default: String,
    ): Flow<String> {
        requests += key to default
        return flowOf(value ?: default)
    }
}

private class SupportAudioPlayer(
    override val isSupported: Boolean,
) : ChapterAudioPlayer {
    override val events: Flow<ChapterAudioPlayerEvent> = emptyFlow()
    override val position: Duration = Duration.ZERO

    override fun play(
        audioPath: String,
        position: Duration,
        speed: Float,
        playbackId: Int,
    ) = error("unused")

    override fun setSpeed(speed: Float) = error("unused")

    override fun pause() = error("unused")

    override fun release() = error("unused")
}
