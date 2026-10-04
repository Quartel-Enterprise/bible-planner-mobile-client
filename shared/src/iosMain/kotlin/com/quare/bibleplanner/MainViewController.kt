package com.quare.bibleplanner

import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource
import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdDataSource
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
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

private var isInitialized = false

/*
 * Why: Koin starts before any UI so the background URLSession handler can reach the graph when
 * the app launches only for background events; repeated calls are ignored.
 */
@OptIn(ExperimentalNativeApi::class)
fun initializeKoinForIos(
    remoteConfigService: RemoteConfigDataSource,
    analyticsService: AnalyticsService,
    crashReporter: CrashReporter,
    downloadSession: IosDownloadSession,
    reviewRequester: IosReviewRequester,
    rewardedAdDataSource: RewardedAdDataSource,
    adsConsentDataSource: AdsConsentDataSource,
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
                    single { rewardedAdDataSource }
                    single { adsConsentDataSource }
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
        /*
         * Why: force-create the facade so setBridge runs now; on a background-only URLSession relaunch
         * there is no UI to request it from Koin.
         */
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
    rewardedAdDataSource: RewardedAdDataSource,
    adsConsentDataSource: AdsConsentDataSource,
): UIViewController = ComposeUIViewController(
    configure = {
        initializeKoinForIos(
            remoteConfigService = remoteConfigService,
            analyticsService = analyticsService,
            crashReporter = crashReporter,
            downloadSession = downloadSession,
            reviewRequester = reviewRequester,
            rewardedAdDataSource = rewardedAdDataSource,
            adsConsentDataSource = adsConsentDataSource,
        )
    },
) {
    val viewController = LocalUIViewController.current
    AppRoot(
        onThemeResolved = { isAppInDarkTheme ->
            val userInterfaceStyle = if (isAppInDarkTheme) {
                UIUserInterfaceStyle.UIUserInterfaceStyleDark
            } else {
                UIUserInterfaceStyle.UIUserInterfaceStyleLight
            }
            viewController.overrideUserInterfaceStyle = userInterfaceStyle
            viewController.view.window?.overrideUserInterfaceStyle = userInterfaceStyle
        },
    )
}

/*
 * Why: called from Swift onOpenURL for the Live Activity Pause/Resume/Cancel buttons; the action
 * strings must match the deep links the widget sends.
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
