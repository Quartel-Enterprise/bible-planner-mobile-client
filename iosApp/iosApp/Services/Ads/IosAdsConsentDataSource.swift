import UIKit
import UserMessagingPlatform
import Shared

final class IosAdsConsentDataSource: AdsConsentDataSource {
    func gatherConsent(onComplete: @escaping (KotlinBoolean) -> Void) {
        ConsentInformation.shared.requestConsentInfoUpdate(with: RequestParameters()) { error in
            if let error {
                dlog("Consent info update failed: \(error.localizedDescription)", tag: "ADS")
                onComplete(KotlinBoolean(value: false))
                return
            }
            MainActor.assumeIsolated {
                ConsentForm.loadAndPresentIfRequired(from: topViewController()) { error in
                    if let error {
                        dlog("Consent form failed: \(error.localizedDescription)", tag: "ADS")
                    }
                    onComplete(KotlinBoolean(value: true))
                }
            }
        }
    }

    func canRequestAds() -> Bool {
        ConsentInformation.shared.canRequestAds
    }

    func isPrivacyOptionsRequired() -> Bool {
        ConsentInformation.shared.privacyOptionsRequirementStatus == .required
    }

    func showPrivacyOptions(onComplete: @escaping () -> Void) {
        MainActor.assumeIsolated {
            ConsentForm.presentPrivacyOptionsForm(from: topViewController()) { error in
                if let error {
                    dlog("Privacy options form failed: \(error.localizedDescription)", tag: "ADS")
                }
                onComplete()
            }
        }
    }
}
