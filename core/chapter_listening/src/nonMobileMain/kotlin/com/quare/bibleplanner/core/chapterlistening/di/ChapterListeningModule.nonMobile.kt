package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.data.service.UnsupportedChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.UnsupportedListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Why: listening is off on these platforms, so the cache directory is never touched and needs no real location yet.
internal actual val platformChapterListeningModule: Module = module {
    single { ChapterAudioCacheDirectory(Path(CHAPTER_AUDIO_DIRECTORY)) }
    singleOf(::UnsupportedChapterAudioPlayer).bind<ChapterAudioPlayer>()
    singleOf(::UnsupportedListeningMediaSession).bind<ListeningMediaSession>()
}
