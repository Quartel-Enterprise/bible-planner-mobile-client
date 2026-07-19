import StoreKit
import UIKit
import Shared

final class StoreKitReviewRequester: IosReviewRequester {
    func requestReview() -> Bool {
        MainActor.assumeIsolated {
            guard let scene = UIApplication.shared.connectedScenes
                .first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene
            else {
                return false
            }
            AppStore.requestReview(in: scene)
            return true
        }
    }
}
