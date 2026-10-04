package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveBooleanRemoteConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class ObserveIsChapterListeningEnabledUseCase(
    private val speechEngine: SpeechEngine,
    private val observeBooleanRemoteConfig: ObserveBooleanRemoteConfig,
) : ObserveIsChapterListeningEnabled {
    override fun invoke(): Flow<Boolean> = if (speechEngine.isSupported) {
        observeBooleanRemoteConfig(
            key = CHAPTER_LISTENING_ENABLED_KEY,
            default = false,
        )
    } else {
        flowOf(false)
    }

    private companion object {
        const val CHAPTER_LISTENING_ENABLED_KEY = "chapter_listening_enabled"
    }
}
