package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioCacheDataSource
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioCacheDataSourceImpl
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioRemoteDataSource
import com.quare.bibleplanner.core.chapterlistening.data.datasource.ChapterAudioRemoteDataSourceImpl
import com.quare.bibleplanner.core.chapterlistening.data.mapper.ChapterAudioMapper
import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterAudioRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterListeningSettingsRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterListeningUnlockRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningControllerImpl
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterAudioRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningTimeUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningWordsUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetTodayListeningDay
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetChapterListeningAccessUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetListeningChapterTitleUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetTodayListeningDayUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.ObserveIsChapterListeningEnabledUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.RecordChapterListeningUnlockUseCase
import com.quare.bibleplanner.core.utils.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val CACHE_MAX_BYTES = 300L * 1024 * 1024
internal const val CHAPTER_AUDIO_DIRECTORY = "chapter_audio"

val chapterListeningModule = module {
    includes(platformChapterListeningModule)
    singleOf(::ChapterListeningSettingsRepositoryImpl).bind<ChapterListeningSettingsRepository>()
    singleOf(::ChapterListeningUnlockRepositoryImpl).bind<ChapterListeningUnlockRepository>()
    // Why: its renewal lock only spends a refresh token once while there is a single instance.
    single<ChapterAudioRemoteDataSource> {
        ChapterAudioRemoteDataSourceImpl(
            functions = get(),
            httpClient = get(),
            auth = get(),
            json = get(),
            checkRemoteSessionState = get(),
            currentTimestampProvider = get(),
        )
    }
    single<ChapterAudioCacheDataSource> {
        ChapterAudioCacheDataSourceImpl(
            directory = get(),
            maxBytes = CACHE_MAX_BYTES,
            json = get(),
            currentTimestampProvider = get(),
            dispatcher = ioDispatcher,
        )
    }
    factoryOf(::ChapterAudioMapper)
    singleOf(::ChapterAudioRepositoryImpl).bind<ChapterAudioRepository>()
    factoryOf(::ListeningDateProvider)
    factoryOf(::GetListeningTimeUseCase)
    factoryOf(::GetListeningWordsUseCase)
    factoryOf(::GetChapterListeningAccessUseCase).bind<GetChapterListeningAccess>()
    factoryOf(::RecordChapterListeningUnlockUseCase).bind<RecordChapterListeningUnlock>()
    factoryOf(::ObserveIsChapterListeningEnabledUseCase).bind<ObserveIsChapterListeningEnabled>()
    factoryOf(::GetTodayListeningDayUseCase).bind<GetTodayListeningDay>()
    factoryOf(::GetListeningChapterTitleUseCase).bind<GetListeningChapterTitle>()
    // Why: the player calls back on its own threads, and the controller's state is only touched on main.
    single<ChapterListeningController> {
        ChapterListeningControllerImpl(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            audioPlayer = get(),
            mediaSession = get(),
            settingsRepository = get(),
            chapterAudioRepository = get(),
            observeAuthenticatedUserId = get(),
            getChapterVerseTexts = get(),
            getChapterVerseHeadings = get(),
            getSelectedVersionIdFlow = get(),
            getChapterListeningAccess = get(),
            getAdjacentListeningChapter = get(),
            getListeningChapterTitle = get(),
            getListeningTime = get(),
            getListeningWords = get(),
            trackEvent = get(),
        )
    }
}

internal expect val platformChapterListeningModule: Module
