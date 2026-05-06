import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        guard identifier == BibleVersionDownloadSession.sessionIdentifier else { return }
        BibleVersionDownloadSession.shared?.backgroundCompletionHandler = completionHandler
    }
}
