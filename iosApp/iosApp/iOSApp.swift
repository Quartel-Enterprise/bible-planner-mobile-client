import SwiftUI
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
    }

    var body: some Scene {
        WindowGroup {
            ContentView(remoteConfigService: remoteConfigService)
        }
    }
}
