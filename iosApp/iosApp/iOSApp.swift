import SwiftUI
import BackgroundTasks
import FirebaseCore
import FirebaseRemoteConfig
import UserNotifications
import ComposeApp

@main
struct iOSApp: App {
    let remoteConfigService: RemoteConfigService

    init() {
        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())

        // Request notification permission
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .sound, .badge]
        ) { _, _ in }

        // Allow notifications to appear while app is in foreground
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared

        // Register the BGProcessingTask identifier
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: BibleInitializationTask.identifier,
            using: nil
        ) { task in
            BibleInitializationTask.handle(task as! BGProcessingTask)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView(remoteConfigService: remoteConfigService)
                .onReceive(
                    NotificationCenter.default.publisher(
                        for: UIApplication.didEnterBackgroundNotification
                    )
                ) { _ in
                    BibleInitializationTask.schedule()
                }
        }
    }
}
