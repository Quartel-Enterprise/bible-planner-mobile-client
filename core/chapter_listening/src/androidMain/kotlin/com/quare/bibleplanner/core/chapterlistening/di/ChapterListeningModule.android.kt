package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.data.service.AndroidSpeechEngine
import com.quare.bibleplanner.core.chapterlistening.data.service.ListeningPlayer
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformChapterListeningModule: Module = module {
    singleOf(::AndroidSpeechEngine).bind<SpeechEngine>()
    singleOf(::ListeningPlayer)
    singleOf(::AndroidListeningMediaSession).bind<ListeningMediaSession>()
}
