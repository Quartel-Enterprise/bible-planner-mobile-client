import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadLiveActivity: BibleDownloadLiveActivity
    let reviewRequester: StoreKitReviewRequester
    let rewardedAdDataSource: IosRewardedAdDataSource
    let adsConsentDataSource: IosAdsConsentDataSource
    let releaseNotesScreenFactory: SwiftUIReleaseNotesScreenFactory

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadLiveActivity: downloadLiveActivity,
            reviewRequester: reviewRequester,
            rewardedAdDataSource: rewardedAdDataSource,
            adsConsentDataSource: adsConsentDataSource,
            releaseNotesScreenFactory: releaseNotesScreenFactory
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigDataSource
    let analyticsService: AnalyticsService
    let crashReporter: CrashReporter
    let downloadLiveActivity: BibleDownloadLiveActivity
    let reviewRequester: StoreKitReviewRequester
    let rewardedAdDataSource: IosRewardedAdDataSource
    let adsConsentDataSource: IosAdsConsentDataSource
    let releaseNotesScreenFactory: SwiftUIReleaseNotesScreenFactory

    var body: some View {
        ComposeView(
            remoteConfigService: remoteConfigService,
            analyticsService: analyticsService,
            crashReporter: crashReporter,
            downloadLiveActivity: downloadLiveActivity,
            reviewRequester: reviewRequester,
            rewardedAdDataSource: rewardedAdDataSource,
            adsConsentDataSource: adsConsentDataSource,
            releaseNotesScreenFactory: releaseNotesScreenFactory
        )
        .ignoresSafeArea()
    }
}
