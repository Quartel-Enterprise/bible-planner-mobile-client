package com.quare.bibleplanner

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibleplanner.shared.generated.resources.Res
import bibleplanner.shared.generated.resources.app_title
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.core.provider.crashlytics.configure
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.language.di.jvmLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.platform.isDebugBuild
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.di.jvmAppLanguageModule
import com.quare.bibleplanner.feature.applanguage.presentation.initAppLocale
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.login.di.jvmLoginModule
import com.quare.bibleplanner.notification.DesktopBibleVersionDownloadNotifier
import com.quare.bibleplanner.window.MacAppAppearance
import com.quare.bibleplanner.window.WindowsTitleBarAppearance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun main() {
    initializeKoin(
        platformModules = listOf(
            jvmAppLanguageModule,
            jvmLanguageProviderModule,
            jvmLoginModule,
            languageProviderModule,
            module {
                single { getDatabaseBuilder() }
                singleOf(::DesktopBibleVersionDownloadNotifier).bind<BibleVersionDownloadNotifier>()
                singleOf(::InProcessBibleVersionDownloader)
                singleOf(::InProcessBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
            },
        ),
    )
    val koin = GlobalContext.get()
    koin.get<CrashReporter>().configure(isDebug = isDebugBuild())
    val getThemeOptionFlow = koin.get<GetThemeOptionFlow>()
    runBlocking {
        initAppLocale(
            getAppLanguageFlow = koin.get(),
            applyLocale = koin.get(),
        )
        MacAppAppearance.setLaunchAppearance(getThemeOptionFlow().first())
    }
    application {
        LaunchedEffect(Unit) {
            MacAppAppearance.follow(getThemeOptionFlow())
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.app_title),
        ) {
            val windowsTitleBar = remember(window) { WindowsTitleBarAppearance(window) }
            AppRoot(onThemeResolved = windowsTitleBar::apply)
        }
    }
}
