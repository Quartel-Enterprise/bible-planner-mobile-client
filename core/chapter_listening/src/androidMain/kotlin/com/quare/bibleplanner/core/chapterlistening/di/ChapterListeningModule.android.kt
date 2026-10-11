package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.data.service.ListeningPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.ListeningPlayerEvents
import com.quare.bibleplanner.core.chapterlistening.data.service.createListeningExoPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.io.files.Path
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val platformChapterListeningModule: Module = module {
    single { ChapterAudioCacheDirectory(Path(androidContext().cacheDir.path, CHAPTER_AUDIO_DIRECTORY)) }
    single { createListeningExoPlayer(androidContext()) }
    singleOf(::ListeningPlayerEvents)
    singleOf(::ListeningPlayer)
    single<ChapterAudioPlayer> { AndroidChapterAudioPlayer(createPlayer = { get() }) }
    single<ListeningMediaSession> {
        AndroidListeningMediaSession(
            context = androidContext(),
            events = get(),
            createPlayer = { get() },
        )
    }
}

internal actual fun createChapterAudioHttpClient(configure: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp, configure)
