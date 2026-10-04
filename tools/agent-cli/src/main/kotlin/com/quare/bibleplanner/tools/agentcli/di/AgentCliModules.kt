package com.quare.bibleplanner.tools.agentcli.di

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.language.di.jvmLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.feature.applanguage.di.jvmAppLanguageModule
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.login.di.jvmLoginModule
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Why: the desktop app's platform modules, minus what reaches outside the process (tray, GA4).
fun createAgentCliModules(log: SessionLog): List<Module> = listOf(
    jvmAppLanguageModule,
    jvmLanguageProviderModule,
    jvmLoginModule,
    languageProviderModule,
    module {
        single { getDatabaseBuilder() }
        single<BibleVersionDownloadNotifier> { LoggingBibleVersionDownloadNotifier(log) }
        singleOf(::InProcessBibleVersionDownloader)
        singleOf(::InProcessBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
        single<AnalyticsService> { RecordingAnalyticsService(log) }
    },
)
