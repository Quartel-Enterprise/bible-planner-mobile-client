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
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetTodayListeningDay
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ObserveIsChapterListeningEnabled
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.RecordChapterListeningUnlock
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetChapterListeningAccessUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetListeningChapterTitleUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.GetTodayListeningDayUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.ObserveIsChapterListeningEnabledUseCase
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl.RecordChapterListeningUnlockUseCase
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

private const val CHAPTER_AUDIO_HTTP_CLIENT = "chapter_audio_http_client"
private const val CONNECT_TIMEOUT_MILLIS = 15_000L
private const val SOCKET_TIMEOUT_MILLIS = 30_000L
private const val CACHE_MAX_BYTES = 300L * 1024 * 1024
internal const val CHAPTER_AUDIO_DIRECTORY = "chapter_audio"

val chapterListeningModule = module {
    includes(platformChapterListeningModule)
    singleOf(::ChapterListeningSettingsRepositoryImpl).bind<ChapterListeningSettingsRepository>()
    singleOf(::ChapterListeningUnlockRepositoryImpl).bind<ChapterListeningUnlockRepository>()
    single(named(CHAPTER_AUDIO_HTTP_CLIENT)) {
        // Why: Android carries two engines, and the timeouts and the length check assume a known one.
        createChapterAudioHttpClient {
            // Why: a long chapter on a slow connection may take minutes, so only a stalled download times out.
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }
        }
    }
    // Why: its renewal lock only spends a refresh token once while there is a single instance.
    single<ChapterAudioRemoteDataSource> {
        ChapterAudioRemoteDataSourceImpl(
            functions = get(),
            httpClient = get(named(CHAPTER_AUDIO_HTTP_CLIENT)),
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
        )
    }
    factoryOf(::ChapterAudioMapper)
    singleOf(::ChapterAudioRepositoryImpl).bind<ChapterAudioRepository>()
    factoryOf(::ListeningDateProvider)
    factoryOf(::GetListeningTimeUseCase)
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
            getSelectedVersionIdFlow = get(),
            getChapterListeningAccess = get(),
            getAdjacentListeningChapter = get(),
            getListeningChapterTitle = get(),
            getListeningTime = get(),
            trackEvent = get(),
        )
    }
}

internal expect val platformChapterListeningModule: Module

internal expect fun createChapterAudioHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient
