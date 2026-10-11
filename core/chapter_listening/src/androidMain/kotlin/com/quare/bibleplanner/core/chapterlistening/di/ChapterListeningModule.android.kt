package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.data.service.ListeningPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.ListeningPlayerEvents
import com.quare.bibleplanner.core.chapterlistening.data.service.createListeningExoPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.io.files.Path
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private const val CHAPTER_AUDIO_DIRECTORY = "chapter_audio"

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
