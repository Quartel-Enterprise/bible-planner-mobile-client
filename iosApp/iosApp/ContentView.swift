import UIKit
import SwiftUI
import Shared

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
    }
}
