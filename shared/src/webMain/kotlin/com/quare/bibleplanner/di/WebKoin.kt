package com.quare.bibleplanner.di

import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.crashlytics.configure
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.language.di.webLanguageProviderModule
import com.quare.bibleplanner.core.provider.platform.isDebugBuild
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.feature.applanguage.di.webAppLanguageModule
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.login.di.webLoginModule
import com.quare.bibleplanner.notification.WebBibleVersionDownloadNotifier
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform

/*
 * Why: the Compose web app (wasmJs) and the React app's JS library (js) start the same graph. A second call is
 * ignored, so each React screen can make sure Koin runs before it builds its ViewModel.
 */
fun initializeKoinForWeb() {
    if (KoinPlatform.getKoinOrNull() != null) return
    initializeKoin(
        platformModules = listOf(
            webAppLanguageModule,
            webLanguageProviderModule,
            webLoginModule,
            languageProviderModule,
            module {
                single { getDatabaseBuilder() }
                singleOf(::WebBibleVersionDownloadNotifier).bind<BibleVersionDownloadNotifier>()
                singleOf(::InProcessBibleVersionDownloader)
                singleOf(::InProcessBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
            },
        ),
    )
    KoinPlatform.getKoin().get<CrashReporter>().configure(isDebug = isDebugBuild())
}
