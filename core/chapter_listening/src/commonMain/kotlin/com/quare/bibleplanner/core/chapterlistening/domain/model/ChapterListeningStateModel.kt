package com.quare.bibleplanner.core.chapterlistening.domain.model

data class ChapterListeningStateModel(
    val session: ListeningSessionModel?,
    val settings: ChapterListeningSettingsModel,
    val sleepTimer: ListeningSleepTimerModel,
    val voices: SpeechVoicesModel,
    val selectedVoiceId: String?,
    val previewingVoiceId: String?,
)
