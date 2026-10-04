package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChapterListeningController {
    val isSupported: Boolean
    val state: StateFlow<ChapterListeningStateModel>
    val events: Flow<ChapterListeningEventModel>

    fun startChapter(
        chapter: ChapterLocationModel,
        shouldForceCanonOrder: Boolean,
    )

    fun startDayReading(
        day: ListeningDayModel,
        chapter: ChapterLocationModel,
    )

    fun followReader(chapter: ChapterLocationModel)

    fun continueLockedChapter()

    fun togglePlayPause()

    fun nextVerse()

    fun previousVerse()

    fun skipToVerse(verseIndex: Int)

    fun nextChapter()

    fun previousChapter()

    suspend fun getAdjacentChapter(direction: ChapterDirectionModel): ChapterLocationModel?

    fun stop()

    fun setSpeed(speed: Float)

    fun selectVoice(voiceId: String)

    fun previewVoice(
        voiceId: String,
        sampleText: String,
    )

    fun setSleepTimer(option: ListeningSleepTimerOption)

    fun setAutoNextEnabled(isEnabled: Boolean)

    fun dismissFinishOffer()

    fun attachReader()

    fun detachReader()

    fun openVoiceSettings()
}
