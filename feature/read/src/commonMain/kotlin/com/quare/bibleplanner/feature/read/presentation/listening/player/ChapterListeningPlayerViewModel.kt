package com.quare.bibleplanner.feature.read.presentation.listening.player

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterDirectionModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ChapterListeningStateModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningModeModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSleepTimerOption
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.read.presentation.listening.ChapterListeningGate
import com.quare.bibleplanner.feature.read.presentation.listening.ListeningPlaybackActions
import com.quare.bibleplanner.feature.read.presentation.listening.mapper.ListeningPlayerUiModelMapper
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningControl
import com.quare.bibleplanner.feature.read.presentation.listening.model.ListeningSurface
import com.quare.bibleplanner.ui.utils.observe
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class ChapterListeningPlayerViewModel(
    private val controller: ChapterListeningController,
    private val gate: ChapterListeningGate,
    private val playbackActions: ListeningPlaybackActions,
    private val navigator: Navigator,
    private val playerUiModelMapper: ListeningPlayerUiModelMapper,
    trackEvent: TrackEvent,
) : TrackedViewModel<ChapterListeningPlayerUiEvent>(trackEvent) {
    private val adjacentChapters = MutableStateFlow<Pair<ChapterLocationModel?, ChapterLocationModel?>>(null to null)

    val uiState: StateFlow<ChapterListeningPlayerUiState> = combine(
        controller.state,
        adjacentChapters,
    ) { state, (previous, next) ->
        toUiState(
            state = state,
            previousChapter = previous,
            nextChapter = next,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = toUiState(
            state = controller.state.value,
            previousChapter = null,
            nextChapter = null,
        ),
    )

    init {
        observe(
            controller.state
                .map { state -> state.session == null }
                .distinctUntilChanged()
                .filter { isStopped -> isStopped },
        ) {
            navigator.navigateBack()
        }
        observe(
            controller.state
                .map { state -> state.session }
                .distinctUntilChanged { old, new -> old?.segment == new?.segment && old?.mode == new?.mode },
        ) { session ->
            adjacentChapters.update { if (session == null) null to null else findAdjacentChapters() }
        }
    }

    override fun handleEvent(event: ChapterListeningPlayerUiEvent) {
        when (event) {
            ChapterListeningPlayerUiEvent.OnPlayPauseClick -> playbackActions.togglePlayPause(ListeningSurface.PLAYER)

            ChapterListeningPlayerUiEvent.OnPreviousVerseClick -> controller.previousVerse()

            ChapterListeningPlayerUiEvent.OnNextVerseClick -> controller.nextVerse()

            ChapterListeningPlayerUiEvent.OnPreviousChapterClick -> openAdjacentChapter(
                direction = ChapterDirectionModel.PREVIOUS,
                chapter = uiState.value.previousChapter,
            )

            ChapterListeningPlayerUiEvent.OnNextChapterClick -> openAdjacentChapter(
                direction = ChapterDirectionModel.NEXT,
                chapter = uiState.value.nextChapter,
            )

            is ChapterListeningPlayerUiEvent.OnSeek -> controller.skipToVerse(event.verseIndex)

            is ChapterListeningPlayerUiEvent.OnSpeedClick -> controller.setSpeed(event.speed)

            ChapterListeningPlayerUiEvent.OnUnavailableActionClick -> playbackActions.handleUnavailableAction(
                scope = viewModelScope,
                surface = ListeningSurface.PLAYER,
            )

            is ChapterListeningPlayerUiEvent.OnSleepTimerClick -> controller.setSleepTimer(event.option)

            is ChapterListeningPlayerUiEvent.OnAutoNextToggle -> controller.setAutoNextEnabled(event.isEnabled)

            ChapterListeningPlayerUiEvent.OnUnlockClick -> playbackActions.unlockLockedChapter(
                scope = viewModelScope,
                surface = ListeningSurface.PLAYER,
            )

            ChapterListeningPlayerUiEvent.OnDismiss -> navigator.navigateBack()
        }
    }

    /*
     * Why: asking before moving keeps the current chapter playing when the next one turns out locked;
     * while paused the move only looks ahead, so the day's unlock is asked for when it is played.
     */
    private fun openAdjacentChapter(
        direction: ChapterDirectionModel,
        chapter: ChapterLocationModel?,
    ) {
        val control = if (direction == ChapterDirectionModel.NEXT) {
            ListeningControl.NEXT_CHAPTER
        } else {
            ListeningControl.PREVIOUS_CHAPTER
        }
        trackEvent(
            name = AnalyticsEventNames.CHAPTER_LISTENING_CONTROL_CLICKED,
            params = mapOf(
                AnalyticsParams.CONTROL to control.key,
                AnalyticsParams.SURFACE to ListeningSurface.PLAYER.key,
            ),
        )
        val target = chapter ?: return
        if (controller.state.value.session
                ?.isPlaying != true
        ) {
            controller.moveToAdjacentChapter(
                direction = direction,
                chapter = target,
                shouldPlay = false,
            )
            return
        }
        // Why: the chapter unlocked is the one opened, even if the session moved on meanwhile.
        gate.request(
            scope = viewModelScope,
            chapter = target,
            onAllowed = {
                controller.moveToAdjacentChapter(
                    direction = direction,
                    chapter = target,
                    shouldPlay = true,
                )
            },
        )
    }

    private suspend fun findAdjacentChapters(): Pair<ChapterLocationModel?, ChapterLocationModel?> =
        controller.getAdjacentChapter(ChapterDirectionModel.PREVIOUS) to
            controller.getAdjacentChapter(ChapterDirectionModel.NEXT)

    private fun toUiState(
        state: ChapterListeningStateModel,
        previousChapter: ChapterLocationModel?,
        nextChapter: ChapterLocationModel?,
    ): ChapterListeningPlayerUiState {
        val session = state.session
        return ChapterListeningPlayerUiState(
            player = session?.let {
                playerUiModelMapper.map(
                    session = it,
                    speed = state.settings.speed,
                )
            },
            previousChapter = previousChapter,
            nextChapter = nextChapter,
            speed = state.settings.speed,
            sleepTimer = toSleepTimer(state.sleepTimer),
            isAutoNextEnabled = state.settings.isAutoNextEnabled || session?.mode == ListeningModeModel.DAY_READING,
            isAutoNextLocked = session?.mode == ListeningModeModel.DAY_READING,
        )
    }

    private fun toSleepTimer(sleepTimer: ListeningSleepTimerModel): ListeningSleepTimerUiModel = when (sleepTimer) {
        ListeningSleepTimerModel.Off -> ListeningSleepTimerUiModel(
            selectedOption = ListeningSleepTimerOption.OFF,
            remaining = null,
        )

        is ListeningSleepTimerModel.Countdown -> ListeningSleepTimerUiModel(
            selectedOption = sleepTimer.option,
            remaining = sleepTimer.remaining,
        )

        ListeningSleepTimerModel.EndOfChapter -> ListeningSleepTimerUiModel(
            selectedOption = ListeningSleepTimerOption.END_OF_CHAPTER,
            remaining = null,
        )
    }
}
