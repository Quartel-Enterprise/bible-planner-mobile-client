package com.quare.bibleplanner.core.chapterlistening.domain.controller

import com.quare.bibleplanner.core.books.domain.usecase.GetChapterVerseContents
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlow
import com.quare.bibleplanner.core.chapterlistening.domain.mapper.ListeningNowPlayingMapper
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.FindAdjacentListeningSegmentUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetAdjacentListeningChapter
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersesUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningWordsUseCase
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.CoroutineScope

/*
 * Why: the controller's collaborators must share one state store, one playback and one scope, so the app
 * and the tests build them through this single function instead of wiring them twice.
 */
internal fun createChapterListeningController(
    scope: CoroutineScope,
    audioPlayer: ChapterAudioPlayer,
    mediaSession: ListeningMediaSession,
    settingsRepository: ChapterListeningSettingsRepository,
    chapterAudioRepository: ChapterAudioRepository,
    getChapterVerseContents: GetChapterVerseContents,
    getSelectedVersionIdFlow: GetSelectedVersionIdFlow,
    getChapterListeningAccess: GetChapterListeningAccess,
    getAdjacentListeningChapter: GetAdjacentListeningChapter,
    getListeningChapterTitle: GetListeningChapterTitle,
    getListeningTime: GetListeningTimeUseCase,
    getListeningWords: GetListeningWordsUseCase,
    trackEvent: TrackEvent,
    observeAuthenticatedUserId: ObserveAuthenticatedUserId,
): ChapterListeningControllerImpl {
    val store = ChapterListeningStateStore()
    val tracker = ChapterListeningAnalyticsTracker(trackEvent)
    val findAdjacentSegment = FindAdjacentListeningSegmentUseCase(getAdjacentListeningChapter)
    val sleepTimer = ListeningSleepTimer(
        scope = scope,
        store = store,
        tracker = tracker,
    )
    val prefetcher = ChapterAudioPrefetcher(
        scope = scope,
        chapterAudioRepository = chapterAudioRepository,
        findAdjacentSegment = findAdjacentSegment,
        getChapterListeningAccess = getChapterListeningAccess,
    )
    val cacheKeeper = ChapterAudioCacheKeeper(
        scope = scope,
        chapterAudioRepository = chapterAudioRepository,
    )
    // Why: the playback reports a segment end to the controller, which itself needs the playback, so it is set right below.
    lateinit var controller: ChapterListeningControllerImpl
    val playback = ListeningPlayback(
        scope = scope,
        audioPlayer = audioPlayer,
        mediaSession = mediaSession,
        store = store,
        sleepTimer = sleepTimer,
        prefetcher = prefetcher,
        tracker = tracker,
        onSegmentEnd = { controller.loadAfterSegmentEnd() },
    )
    controller = ChapterListeningControllerImpl(
        scope = scope,
        store = store,
        playback = playback,
        loader = ListeningSegmentLoader(
            store = store,
            playback = playback,
            prefetcher = prefetcher,
            cacheKeeper = cacheKeeper,
            chapterAudioRepository = chapterAudioRepository,
            getListeningVerses = GetListeningVersesUseCase(
                getChapterVerseContents = getChapterVerseContents,
                getListeningWords = getListeningWords,
            ),
            tracker = tracker,
        ),
        sleepTimer = sleepTimer,
        prefetcher = prefetcher,
        cacheKeeper = cacheKeeper,
        readerAttachments = ListeningReaderAttachments(scope),
        mediaSession = mediaSession,
        settingsRepository = settingsRepository,
        getSelectedVersionIdFlow = getSelectedVersionIdFlow,
        getChapterListeningAccess = getChapterListeningAccess,
        findAdjacentSegment = findAdjacentSegment,
        tracker = tracker,
        nowPlayingMapper = ListeningNowPlayingMapper(
            getListeningChapterTitle = getListeningChapterTitle,
            getListeningTime = getListeningTime,
        ),
        observeAuthenticatedUserId = observeAuthenticatedUserId,
    )
    return controller
}
