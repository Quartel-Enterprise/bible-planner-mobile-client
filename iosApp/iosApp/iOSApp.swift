import SwiftUI
import BackgroundTasks
import FirebaseCore
import FirebaseRemoteConfig
import UserNotifications
import ComposeApp

@main
struct iOSApp: App {
    let remoteConfigService: RemoteConfigService
    let backgroundTaskScheduler: IosBackgroundTaskSchedulerImpl

    init() {
        backgroundTaskScheduler = IosBackgroundTaskSchedulerImpl()

        // Register BGTask handler before the app finishes launching (Apple requirement)
        BibleVersionDownloadTaskHandler.register(scheduler: backgroundTaskScheduler)

        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())

        // Initialize Koin early so the BGTask handler can access the Koin graph
        // even when the app is launched solely to execute a background task.
        MainViewControllerKt.initializeKoinForIos(
            remoteConfigService: remoteConfigService,
            bgTaskScheduler: backgroundTaskScheduler
        )

        // Request notification permission
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { _, _ in }

        // Allow notifications to appear while app is in foreground
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                remoteConfigService: remoteConfigService,
                backgroundTaskScheduler: backgroundTaskScheduler
            )
        }
    }
}
