package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechEngineEvent
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechUtteranceModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ObserveIsChapterListeningEnabledUseCaseTest {
    private lateinit var useCase: ObserveIsChapterListeningEnabledUseCase
    private lateinit var remoteConfig: FakeObserveBooleanRemoteConfig

    @Test
    fun `GIVEN a platform without a voice engine WHEN observing THEN listening is off`() = runTest {
        // Given
        prepareScenario(
            isSupported = false,
            remoteValue = true,
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
            remoteValue = true,
        )

        // When
        val isEnabled = useCase().first()

        // Then
        assertTrue(isEnabled)
        assertEquals("chapter_listening_enabled" to false, remoteConfig.requests.single())
    }

    private fun prepareScenario(
        isSupported: Boolean,
        remoteValue: Boolean,
    ) {
        remoteConfig = FakeObserveBooleanRemoteConfig(remoteValue)
        useCase = ObserveIsChapterListeningEnabledUseCase(
            speechEngine = SupportSpeechEngine(isSupported),
            observeBooleanRemoteConfig = remoteConfig,
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

private class SupportSpeechEngine(
    override val isSupported: Boolean,
) : SpeechEngine {
    override val events: Flow<SpeechEngineEvent> = emptyFlow()

    override suspend fun loadVoices(languageTag: String): SpeechVoicesModel = error("unused")

    override fun speak(
        utterances: List<SpeechUtteranceModel>,
        voiceId: String?,
        languageTag: String,
        speed: Float,
    ) = error("unused")

    override fun stop() = error("unused")

    override fun openVoiceSettings() = error("unused")
}
