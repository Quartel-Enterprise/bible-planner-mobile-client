package com.quare.bibleplanner

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.billing.configureRevenueCat
import com.quare.bibleplanner.core.provider.crashlytics.configure
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.provider.language.di.iosLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.core.provider.platform.domain.usecase.RequestInAppReview
import com.quare.bibleplanner.core.provider.room.db.getDatabaseBuilder
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.di.iosAppLanguageModule
import com.quare.bibleplanner.feature.login.di.iosLoginModule
import com.quare.bibleplanner.notification.IosBibleVersionDownloadNotifier
import com.quare.bibleplanner.review.IosReviewRequester
import com.quare.bibleplanner.worker.IosBackgroundDownloadBridge
import com.quare.bibleplanner.worker.IosBibleVersionDownloaderFacade
import com.quare.bibleplanner.worker.IosDownloadSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.experimental.ExperimentalNativeApi

private var isInitialized = false

/**
 * Initializes Koin early — before the UI is shown — so that the URLSession background download
 * handler can access the Koin graph even when the app is launched solely for background events.
 * Safe to call multiple times; only the first call takes effect.
 */
@OptIn(ExperimentalNativeApi::class)
fun initializeKoinForIos(
    remoteConfigService: RemoteConfigDataSource,
    analyticsService: AnalyticsService,
    crashReporter: CrashReporter,
    downloadSession: IosDownloadSession,
    reviewRequester: IosReviewRequester,
) {
    if (isInitialized) return
    try {
        initializeKoin(
            platformModules = listOf(
                iosAppLanguageModule,
                iosLanguageProviderModule,
                iosLoginModule,
                languageProviderModule,
                module {
                    single { getDatabaseBuilder() }
                    single { remoteConfigService }
                    single { analyticsService }
                    single { crashReporter }
                    single { downloadSession }.bind<IosDownloadSession>()
                    factory<RequestInAppReview> {
                        RequestInAppReview {
                            withContext(Dispatchers.Main) { reviewRequester.requestReview() }
                        }
                    }
                    single { IosBibleVersionDownloadNotifier(get()) }.bind<BibleVersionDownloadNotifier>()
                    singleOf(::IosBackgroundDownloadBridge)
                    singleOf(::IosBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
                },
            ),
        )
        // Force-create the facade so setBridge is called immediately.
        // Required when the app relaunches solely for background URLSession events
        // (no UI = no one else requests the facade from Koin).
        KoinPlatform.getKoin().get<BibleVersionDownloaderFacade>()
        configureRevenueCat(isDebug = Platform.isDebugBinary)
        crashReporter.configure(isDebug = Platform.isDebugBinary)
        isInitialized = true
    } catch (e: Exception) {
        Logger.e(e) { "Error initializing Koin for iOS" }
    }
}

fun MainViewController(
    remoteConfigService: RemoteConfigDataSource,
    analyticsService: AnalyticsService,
    crashReporter: CrashReporter,
    downloadSession: IosDownloadSession,
    reviewRequester: IosReviewRequester,
) = ComposeUIViewController(
    configure = {
        initializeKoinForIos(
            remoteConfigService = remoteConfigService,
            analyticsService = analyticsService,
            crashReporter = crashReporter,
            downloadSession = downloadSession,
            reviewRequester = reviewRequester,
        )
    },
) { App() }

/**
 * Routes a deep link action from the Live Activity buttons to the download facade.
 * Called by Swift via onOpenURL when the user taps Pause / Resume / Cancel.
 *
 * @param action One of: "pause", "resume", "cancel"
 * @param versionId The Bible version ID (e.g. "nvi")
 */
fun handleDownloadAction(
    action: String,
    versionId: String,
) {
    val facade = KoinPlatform.getKoin().get<BibleVersionDownloaderFacade>()
    CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
        when (action) {
            "pause" -> facade.pauseDownload(versionId)
            "resume" -> facade.downloadVersion(versionId)
            "cancel" -> facade.deleteDownload(versionId)
        }
    }
}
