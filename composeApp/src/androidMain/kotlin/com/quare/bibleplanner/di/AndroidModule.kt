package com.quare.bibleplanner.di

import com.quare.bibleplanner.MainActivityViewModel
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.notification.AndroidBibleVersionDownloadNotifier
import com.quare.bibleplanner.notification.BibleVersionNotificationFactory
import com.quare.bibleplanner.worker.AndroidBibleVersionDownloadRequestFactory
import com.quare.bibleplanner.worker.AndroidBibleVersionDownloaderFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidModule = module {
    single { getDatabaseBuilder(androidContext()) }
    viewModelOf(::MainActivityViewModel)
    single { BibleVersionNotificationFactory(androidContext()) }
    single { AndroidBibleVersionDownloadNotifier(androidContext(), get()) }.bind<BibleVersionDownloadNotifier>()
    single {
        AndroidBibleVersionDownloaderFacade(
            context = androidContext(),
            bibleVersionDao = get(),
            verseDao = get(),
            notifier = get(),
            requestFactory = AndroidBibleVersionDownloadRequestFactory(),
        )
    }.bind<BibleVersionDownloaderFacade>()
}
