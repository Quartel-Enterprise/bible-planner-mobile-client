import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigService
    let downloadSession: BibleVersionDownloadSession

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            remoteConfigService: remoteConfigService,
            downloadSession: downloadSession
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigService
    let downloadSession: BibleVersionDownloadSession

    var body: some View {
        ComposeView(
            remoteConfigService: remoteConfigService,
            downloadSession: downloadSession
        )
        .ignoresSafeArea()
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
