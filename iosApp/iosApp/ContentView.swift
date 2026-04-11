import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigService
    let backgroundTaskScheduler: IosBackgroundTaskSchedulerImpl

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            remoteConfigService: remoteConfigService,
            bgTaskScheduler: backgroundTaskScheduler
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigService
    let backgroundTaskScheduler: IosBackgroundTaskSchedulerImpl

    var body: some View {
        ComposeView(
            remoteConfigService: remoteConfigService,
            backgroundTaskScheduler: backgroundTaskScheduler
        )
        .ignoresSafeArea()
    }
}
