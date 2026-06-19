import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let downloadSession: BibleVersionDownloadSession

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            downloadSession: downloadSession
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let downloadSession: BibleVersionDownloadSession

    var body: some View {
        ComposeView(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            downloadSession: downloadSession
        )
        .ignoresSafeArea()
    }
}
