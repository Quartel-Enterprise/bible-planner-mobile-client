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

// MARK: - BGProcessingTask handler

private enum BibleInitializationTask {
    static let identifier = "com.quare.bibleplanner.bible_initialization"

    static func schedule() {
        let request = BGProcessingTaskRequest(identifier: identifier)
        request.requiresNetworkConnectivity = false
        request.requiresExternalPower = false
        try? BGTaskScheduler.shared.submit(request)
    }

    static func handle(_ task: BGProcessingTask) {
        let worker = IosBibleInitializationWorker()

        task.expirationHandler = {
            worker.cancel()
            task.setTaskCompleted(success: false)
        }

        worker.start { success in
            task.setTaskCompleted(success: success)
        }
    }
}

// MARK: - Show notifications while app is in foreground

private class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler:
            @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }
}
