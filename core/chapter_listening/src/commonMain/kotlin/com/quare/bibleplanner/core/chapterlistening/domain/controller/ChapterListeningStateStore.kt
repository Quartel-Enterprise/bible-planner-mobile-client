package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningEventModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningSettingsModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSessionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Why: the controller, its playback, loader and sleep timer all read and write this one state and its events.
internal class ChapterListeningStateStore {
    val state: StateFlow<ChapterListeningStateModel>
        field = MutableStateFlow<ChapterListeningStateModel>(
            ChapterListeningStateModel(
                session = null,
                settings = ChapterListeningSettingsModel(
                    speed = ChapterListeningSettingsModel.DEFAULT_SPEED,
                    isAutoNextEnabled = true,
                    isWordHighlightEnabled = true,
                ),
                sleepTimer = ListeningSleepTimerModel.Off,
            ),
        )

    val events: SharedFlow<ChapterListeningEventModel>
        field = MutableSharedFlow<ChapterListeningEventModel>(extraBufferCapacity = 1)

    val session: ListeningSessionModel?
        get() = state.value.session

    val settings: ChapterListeningSettingsModel
        get() = state.value.settings

    val sleepTimer: ListeningSleepTimerModel
        get() = state.value.sleepTimer

    fun update(transform: (ChapterListeningStateModel) -> ChapterListeningStateModel) {
        state.update(transform)
    }

    suspend fun emit(event: ChapterListeningEventModel) {
        events.emit(event)
    }

    fun updateSession(transform: (ListeningSessionModel) -> ListeningSessionModel) {
        state.update { current -> current.copy(session = current.session?.let(transform)) }
    }
}
