package com.quare.bibleplanner.core.chapterlistening.di

import com.quare.bibleplanner.core.chapterlistening.data.service.IosListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.data.service.IosSpeechEngine
import com.quare.bibleplanner.core.chapterlistening.domain.service.ListeningMediaSession
import com.quare.bibleplanner.core.chapterlistening.domain.service.SpeechEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformChapterListeningModule: Module = module {
    singleOf(::IosSpeechEngine).bind<SpeechEngine>()
    singleOf(::IosListeningMediaSession).bind<ListeningMediaSession>()
}
