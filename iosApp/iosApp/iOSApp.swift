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

    init() {
        let downloadSession = BibleVersionDownloadSession()
        self.downloadSession = downloadSession

        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())
        analyticsService = IosAnalyticsService()
        crashReporter = IosCrashReporter()

        // Initialize Koin early so background URLSession events can access the Koin graph
        // even when the app is launched solely to process background download events.
        MainViewControllerKt.initializeKoinForIos(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadSession: downloadSession
        )

        dlog("Koin initialized", tag: "INIT")

        // Request notification permission
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { granted, error in
            dlog("Notification permission — granted: \(granted), error: \(String(describing: error))", tag: "NOTIF")
        }

        // Allow notifications to appear while app is in foreground
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                remoteConfigService: remoteConfigService,
                analyticsService: analyticsService,
                crashReporter: crashReporter,
                downloadSession: downloadSession
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
