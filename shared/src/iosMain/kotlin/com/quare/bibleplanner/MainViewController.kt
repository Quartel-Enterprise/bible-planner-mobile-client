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
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloaderFacade
import com.quare.bibleplanner.feature.login.di.iosLoginModule
import com.quare.bibleplanner.notification.IosBibleVersionDownloadNotifier
import com.quare.bibleplanner.notification.IosDownloadLiveActivity
import com.quare.bibleplanner.review.IosReviewRequester
import com.quare.bibleplanner.worker.IosBibleVersionDownloaderFacade
import com.quare.bibleplanner.worker.IosDownloadBackgroundTask
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

// Why: the App init and the Compose controller both call this; repeated calls are ignored.
@OptIn(ExperimentalNativeApi::class)
fun initializeKoinForIos(
    remoteConfigService: RemoteConfigDataSource,
    analyticsService: AnalyticsService,
    crashReporter: CrashReporter,
    downloadLiveActivity: IosDownloadLiveActivity,
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
                    single { downloadLiveActivity }
                    factory<RequestInAppReview> {
                        RequestInAppReview {
                            withContext(Dispatchers.Main) { reviewRequester.requestReview() }
                        }
                    }
                    singleOf(::IosBibleVersionDownloadNotifier).bind<BibleVersionDownloadNotifier>()
                    singleOf(::InProcessBibleVersionDownloader)
                    singleOf(::InProcessBibleVersionDownloaderFacade)
                    singleOf(::IosDownloadBackgroundTask)
                    singleOf(::IosBibleVersionDownloaderFacade).bind<BibleVersionDownloaderFacade>()
                },
            ),
        )
        KoinPlatform.getKoin().get<IosBibleVersionDownloaderFacade>().startResumingInterruptedDownloads()
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
    downloadLiveActivity: IosDownloadLiveActivity,
    reviewRequester: IosReviewRequester,
    rewardedAdDataSource: RewardedAdDataSource,
    adsConsentDataSource: AdsConsentDataSource,
): UIViewController = ComposeUIViewController(
    configure = {
        initializeKoinForIos(
            remoteConfigService = remoteConfigService,
            analyticsService = analyticsService,
            crashReporter = crashReporter,
            downloadLiveActivity = downloadLiveActivity,
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
    // Why: the Live Activity of an earlier run can send its buttons before Koin starts at launch.
    val facade = KoinPlatform.getKoinOrNull()?.get<BibleVersionDownloaderFacade>() ?: return
    CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
        when (action) {
            "pause" -> facade.pauseDownload(versionId)
            "resume" -> facade.downloadVersion(versionId)
            "cancel" -> facade.deleteDownload(versionId)
        }
    }
}
