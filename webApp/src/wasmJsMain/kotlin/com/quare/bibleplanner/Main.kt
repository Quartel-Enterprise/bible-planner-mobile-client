package com.quare.bibleplanner

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.crashlytics.configure
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.language.di.webLanguageProviderModule
import com.quare.bibleplanner.core.provider.platform.isDebugBuild
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.di.webAppLanguageModule
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.login.di.webLoginModule
import com.quare.bibleplanner.notification.WebBibleVersionDownloadNotifier
import com.quare.bibleplanner.worker.WebBibleVersionDownloaderFacade
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
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
                singleOf(::WebBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
            },
        ),
    )
    GlobalContext.get().get<CrashReporter>().configure(isDebug = isDebugBuild())
    ComposeViewport(
        configure = { isA11YEnabled = true },
    ) {
        AppRoot()
    }
}
