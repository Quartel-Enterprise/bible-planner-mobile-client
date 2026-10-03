import GoogleMobileAds
import UIKit
import Shared

final class IosRewardedAdDataSource: NSObject, RewardedAdDataSource, FullScreenContentDelegate {
    private var rewardedAd: RewardedAd?
    private var isStarted = false
    private var onDismissed: (() -> Void)?
    private var onFailedToShow: (() -> Void)?

    var isSupported: Bool { true }

    func load(
        adUnitId: String,
        onLoaded: @escaping () -> Void,
        onFailed: @escaping (RewardedAdFailureReason) -> Void
    ) {
        startOnce()
        RewardedAd.load(with: adUnitId, request: Request()) { [weak self] ad, error in
            guard let ad else {
                self?.rewardedAd = nil
                dlog("Rewarded ad failed to load: \(error?.localizedDescription ?? "unknown")", tag: "ADS")
                let isNoFill = (error as? RequestError)?.code == .noFill
                onFailed(isNoFill ? .noFill : .loadError)
                return
            }
            self?.rewardedAd = ad
            onLoaded()
        }
    }

    func show(
        onEarned: @escaping () -> Void,
        onDismissed: @escaping () -> Void,
        onFailed: @escaping () -> Void
    ) {
        MainActor.assumeIsolated {
            guard let ad = rewardedAd, let viewController = topViewController() else {
                onFailed()
                return
            }
            rewardedAd = nil
            self.onDismissed = onDismissed
            onFailedToShow = onFailed
            ad.fullScreenContentDelegate = self
            ad.present(from: viewController) {
                onEarned()
            }
        }
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        onDismissed?()
        clearShowCallbacks()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        dlog("Rewarded ad failed to show: \(error.localizedDescription)", tag: "ADS")
        onFailedToShow?()
        clearShowCallbacks()
    }

    private func clearShowCallbacks() {
        onDismissed = nil
        onFailedToShow = nil
    }

    private func startOnce() {
        guard !isStarted else { return }
        isStarted = true
        MobileAds.shared.start(completionHandler: nil)
    }
}

@MainActor
func topViewController() -> UIViewController? {
    let scene = UIApplication.shared.connectedScenes
        .first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene
    var top = scene?.keyWindow?.rootViewController
    while let presented = top?.presentedViewController {
        top = presented
    }
    return top
}
