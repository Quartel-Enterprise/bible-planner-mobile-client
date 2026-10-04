package com.quare.bibleplanner.feature.read.fake

import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.chapterlistening.domain.model.SpeechVoicesModel
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class FakeChapterListeningController(
    private val readingOrder: List<ChapterLocationModel> = emptyList(),
    session: ListeningSessionModel? = null,
) : ChapterListeningController {
    override val isSupported: Boolean = true
    override val state = MutableStateFlow(
        ChapterListeningStateModel(
            session = session,
            settings = ChapterListeningSettingsModel(
                speed = 1f,
                voiceId = null,
                isAutoNextEnabled = true,
            ),
            sleepTimer = ListeningSleepTimerModel.Off,
            voices = SpeechVoicesModel.Loading,
            selectedVoiceId = null,
            previewingVoiceId = null,
        ),
    )
    override val events = MutableSharedFlow<ChapterListeningEventModel>(extraBufferCapacity = 1)

    val calls = mutableListOf<String>()
    val startedChapters = mutableListOf<Pair<ChapterLocationModel, Boolean>>()
    val startedDays = mutableListOf<Pair<ListeningDayModel, ChapterLocationModel>>()
    val followedChapters = mutableListOf<ChapterLocationModel>()
    var attachedReaders = 0

    fun setSession(session: ListeningSessionModel?) {
        state.update { it.copy(session = session) }
    }

    override fun startChapter(
        chapter: ChapterLocationModel,
        shouldForceCanonOrder: Boolean,
    ) {
        startedChapters += chapter to shouldForceCanonOrder
    }

    override fun startDayReading(
        day: ListeningDayModel,
        chapter: ChapterLocationModel,
    ) {
        startedDays += day to chapter
    }

    override fun followReader(chapter: ChapterLocationModel) {
        followedChapters += chapter
    }

    override fun continueLockedChapter() {
        calls += "continueLockedChapter"
    }

    override fun togglePlayPause() {
        calls += "togglePlayPause"
    }

    override fun nextVerse() {
        calls += "nextVerse"
    }

    override fun previousVerse() {
        calls += "previousVerse"
    }

    override fun skipToVerse(verseIndex: Int) {
        calls += "skipToVerse:$verseIndex"
    }

    override fun nextChapter() {
        calls += "nextChapter"
    }

    override fun previousChapter() {
        calls += "previousChapter"
    }

    override suspend fun getAdjacentChapter(direction: ChapterDirectionModel): ChapterLocationModel? {
        val chapter = state.value.session
            ?.segment
            ?.chapter ?: return null
        val step = if (direction == ChapterDirectionModel.NEXT) 1 else -1
        return readingOrder.getOrNull(readingOrder.indexOf(chapter) + step)
    }

    override fun stop() {
        calls += "stop"
        setSession(null)
    }

    override fun setSpeed(speed: Float) {
        calls += "setSpeed:$speed"
    }

    override fun selectVoice(voiceId: String) {
        calls += "selectVoice:$voiceId"
    }

    override fun previewVoice(
        voiceId: String,
        sampleText: String,
    ) {
        calls += "previewVoice:$voiceId:$sampleText"
    }

    override fun setSleepTimer(option: ListeningSleepTimerOption) {
        calls += "setSleepTimer:${option.key}"
    }

    override fun setAutoNextEnabled(isEnabled: Boolean) {
        calls += "setAutoNextEnabled:$isEnabled"
    }

    override fun dismissFinishOffer() {
        calls += "dismissFinishOffer"
    }

    override fun attachReader() {
        attachedReaders++
    }

    override fun detachReader() {
        attachedReaders--
    }

    override fun openVoiceSettings() {
        calls += "openVoiceSettings"
    }
}
