package com.quare.bibleplanner

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibleplanner.composeapp.generated.resources.Res
import bibleplanner.composeapp.generated.resources.app_title
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.di.jvmAppLanguageModule
import com.quare.bibleplanner.feature.applanguage.presentation.initAppLocale
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.notification.DesktopBibleVersionDownloadNotifier
import com.quare.bibleplanner.worker.DesktopBibleVersionDownloaderFacade
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun main() = application {
    initializeKoin(
        platformModules = listOf(
            jvmAppLanguageModule,
            module {
                single { getDatabaseBuilder() }
                singleOf(::DesktopBibleVersionDownloadNotifier).bind<BibleVersionDownloadNotifier>()
                singleOf(::InProcessBibleVersionDownloader)
                singleOf(::DesktopBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
            },
        ),
    )
    runBlocking {
        val koin = GlobalContext.get()
        initAppLocale(
            getAppLanguageFlow = koin.get(),
            applyLocale = koin.get(),
        )
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_title),
    ) {
        App()
    }
}
