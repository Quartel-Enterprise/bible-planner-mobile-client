import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadSession: BibleVersionDownloadSession
    let reviewRequester: StoreKitReviewRequester

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadSession: downloadSession,
            reviewRequester: reviewRequester
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadSession: BibleVersionDownloadSession
    let reviewRequester: StoreKitReviewRequester

    var body: some View {
        ComposeView(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadSession: downloadSession,
            reviewRequester: reviewRequester
        )
        .ignoresSafeArea()
    }
}
