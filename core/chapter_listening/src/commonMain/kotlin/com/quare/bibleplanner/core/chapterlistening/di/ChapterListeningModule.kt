package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterListeningSettingsRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.data.repository.ChapterListeningUnlockRepositoryImpl
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningController
import com.quare.bibleplanner.core.chapterlistening.domain.controller.ChapterListeningControllerImpl
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningSettingsRepository
import com.quare.bibleplanner.core.chapterlistening.domain.repository.ChapterListeningUnlockRepository
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.EstimateListeningTimeUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetChapterListeningAccess
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningChapterTitle
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetListeningVersion
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetTodayListeningDay
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetChapterListeningAccessUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetListeningChapterTitleUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetListeningVersionUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetTodayListeningDayUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.ObserveIsChapterListeningEnabledUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.RecordChapterListeningUnlockUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chapterListeningModule = module {
    includes(platformChapterListeningModule)
    singleOf(::ChapterListeningSettingsRepositoryImpl).bind<ChapterListeningSettingsRepository>()
    singleOf(::ChapterListeningUnlockRepositoryImpl).bind<ChapterListeningUnlockRepository>()
    factoryOf(::ListeningDateProvider)
    factoryOf(::EstimateListeningTimeUseCase)
    factoryOf(::GetChapterListeningAccessUseCase).bind<GetChapterListeningAccess>()
    factoryOf(::RecordChapterListeningUnlockUseCase).bind<RecordChapterListeningUnlock>()
    factoryOf(::ObserveIsChapterListeningEnabledUseCase).bind<ObserveIsChapterListeningEnabled>()
    factoryOf(::GetTodayListeningDayUseCase).bind<GetTodayListeningDay>()
    factoryOf(::GetListeningChapterTitleUseCase).bind<GetListeningChapterTitle>()
    factoryOf(::GetListeningVersionUseCase).bind<GetListeningVersion>()
    // Why: the engines call back on their own threads, and the controller's state is only touched on main.
    single<ChapterListeningController> {
        ChapterListeningControllerImpl(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            speechEngine = get(),
            mediaSession = get(),
            settingsRepository = get(),
            getChapterVerseTexts = get(),
            getListeningVersion = get(),
            getChapterListeningAccess = get(),
            getAdjacentListeningChapter = get(),
            getListeningChapterTitle = get(),
            estimateListeningTime = get(),
            trackEvent = get(),
        )
    }
}

internal expect val platformChapterListeningModule: Module
