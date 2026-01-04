import SwiftUI
import FirebaseCore
import FirebaseRemoteConfig
import ComposeApp

@main
struct iOSApp: App {
    let remoteConfigService: RemoteConfigService

    init() {
        FirebaseApp.configure()
        remoteConfigService = IosRemoteConfigService(remoteConfig: RemoteConfig.remoteConfig())
    }

    var body: some Scene {
        WindowGroup {
            ContentView(remoteConfigService: remoteConfigService)
        }
    }
}
