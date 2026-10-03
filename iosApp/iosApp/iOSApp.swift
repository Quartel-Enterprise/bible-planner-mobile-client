import SwiftUI
import FirebaseCore
import FirebaseRemoteConfig
import UserNotifications
import Shared

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadSession: BibleVersionDownloadSession
    let reviewRequester: StoreKitReviewRequester
    let rewardedAdDataSource: IosRewardedAdDataSource
    let adsConsentDataSource: IosAdsConsentDataSource

    init() {
        let downloadSession = BibleVersionDownloadSession()
        self.downloadSession = downloadSession

        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())
        analyticsService = IosAnalyticsService()
        crashReporter = IosCrashReporter()
        reviewRequester = StoreKitReviewRequester()
        rewardedAdDataSource = IosRewardedAdDataSource()
        adsConsentDataSource = IosAdsConsentDataSource()

        // Initialize Koin early so background URLSession events can access the Koin graph
        // even when the app is launched solely to process background download events.
        MainViewControllerKt.initializeKoinForIos(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadSession: downloadSession,
            reviewRequester: reviewRequester,
            rewardedAdDataSource: rewardedAdDataSource,
            adsConsentDataSource: adsConsentDataSource
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
                downloadSession: downloadSession,
                reviewRequester: reviewRequester,
                rewardedAdDataSource: rewardedAdDataSource,
                adsConsentDataSource: adsConsentDataSource
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
