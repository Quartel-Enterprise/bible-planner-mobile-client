package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.model.ChapterAudioCacheDirectory
import com.quare.bibleplanner.core.chapterlistening.data.service.IosChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.data.service.IosListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.ChapterAudioPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import kotlinx.io.files.Path
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private const val CHAPTER_AUDIO_DIRECTORY = "chapter_audio"

internal actual val platformChapterListeningModule: Module = module {
    single {
        val caches = NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).first() as String
        ChapterAudioCacheDirectory(Path(caches, CHAPTER_AUDIO_DIRECTORY))
    }
    singleOf(::IosChapterAudioPlayer).bind<ChapterAudioPlayer>()
    singleOf(::IosListeningMediaSession).bind<ListeningMediaSession>()
}
