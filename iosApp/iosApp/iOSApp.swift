import SwiftUI
import FirebaseCore
import FirebaseRemoteConfig
import UserNotifications
import Shared

@main
struct iOSApp: App {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadLiveActivity: BibleDownloadLiveActivity
    let reviewRequester: StoreKitReviewRequester
    let rewardedAdDataSource: IosRewardedAdDataSource
    let adsConsentDataSource: IosAdsConsentDataSource
    let releaseNotesScreenFactory: SwiftUIReleaseNotesScreenFactory

    init() {
        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())
        analyticsService = IosAnalyticsService()
        crashReporter = IosCrashReporter()
        reviewRequester = StoreKitReviewRequester()
        rewardedAdDataSource = IosRewardedAdDataSource()
        adsConsentDataSource = IosAdsConsentDataSource()
        releaseNotesScreenFactory = SwiftUIReleaseNotesScreenFactory()
        downloadLiveActivity = BibleDownloadLiveActivity()
        LegacyBibleDownloadSession.cancelLeftoverTasks()

        // Initialize Koin before any UI so a Bible download cut off by iOS ending the app resumes at launch.
        MainViewControllerKt.initializeKoinForIos(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadLiveActivity: downloadLiveActivity,
            reviewRequester: reviewRequester,
            rewardedAdDataSource: rewardedAdDataSource,
            adsConsentDataSource: adsConsentDataSource,
            releaseNotesScreenFactory: releaseNotesScreenFactory
        )

        dlog("Koin initialized", tag: "INIT")

        // Allow notifications to appear while app is in foreground
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                remoteConfigService: remoteConfigService,
                analyticsService: analyticsService,
                crashReporter: crashReporter,
                downloadLiveActivity: downloadLiveActivity,
                reviewRequester: reviewRequester,
                rewardedAdDataSource: rewardedAdDataSource,
                adsConsentDataSource: adsConsentDataSource,
                releaseNotesScreenFactory: releaseNotesScreenFactory
            )
            .onOpenURL { url in
                guard url.scheme == "bibleplanner" else { return }
                if url.host == "download" {
                    let parts = url.pathComponents.filter { $0 != "/" }
                    guard parts.count == 2 else { return }
                    MainViewControllerKt.handleDownloadAction(action: parts[0], versionId: parts[1])
                } else if url.host == "navigate" {
                    let path = url.pathComponents.filter { $0 != "/" }.first
                    if path == "bible-versions" {
                        NotificationTapRouter.shared.routeToBibleVersions()
                    }
                }
            }
        }
    }
}
